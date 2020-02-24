package org.shelltest.service.controller;

import org.apache.ibatis.annotations.Param;
import org.jetbrains.annotations.NotNull;
import org.shelltest.service.dto.BackupEntity;
import org.shelltest.service.dto.BuildEntity;
import org.shelltest.service.dto.RollbackFrontendDTO;
import org.shelltest.service.entity.*;
import org.shelltest.service.exception.MyException;
import org.shelltest.service.exception.PackingException;
import org.shelltest.service.mapper.PropertyMapper;
import org.shelltest.service.dto.BuildDTO;
import org.shelltest.service.services.*;
import org.shelltest.service.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/frontend")
public class FrontendController {
    @Autowired
    UploadService uploadService;
    @Autowired
    PropertyService propertyService;
    @Autowired
    BuildAppService buildAppService;
    @Autowired
    RepoService repoService;
    @Autowired
    LoginAuth loginAuth;
    @Autowired
    HttpServletRequest request;
    @Autowired
    OtherUtil otherUtil;


    @Autowired
    PropertyMapper propertyMapper;

    @Value("${local.url}")
    String localURL;
    @Value("${local.username}")
    String localUsername;
    @Value("${local.password}")
    String localPassword;
    @Value("${local.path.git}")
    String localGitPath;
    @Value("${local.path.user}")
    String userPath;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 获取所有前端服务器列表.
     * 是否要对每个用户做可打包权限的控制？目前没有该想法（不难做，不想做）
     * 一个ip只能有一套配置，即目前的架构无法完成同主机多账户部署
     * */
    @GetMapping("/getServerList")
    public ResponseEntity getServerList() {
        List<String> authServers = otherUtil.getGrantedServerList(Constant.PropertyKey.FRONTEND);
        return new ResponseBuilder().putItem("list",authServers).getResponseEntity();
    }

    /**
     * 获取目标主机上可用的历史备份.
     * 备份根文件夹/应用名（webapps下文件夹名）/时间戳/index.html
     * @param serverIP 目标主机
     * */
    @GetMapping("/getAvailBackup")
    public ResponseEntity  getAvailFrontendBackup(@Param("serverIP")  String serverIP) throws MyException {
        PropertyExample serverExample = new PropertyExample();
        serverExample.createCriteria().andKeyEqualTo(serverIP);
        List<Property> serverInfo = propertyMapper.selectByExample(serverExample);
        String backupPath = propertyService.getValueByType(serverInfo, Constant.PropertyType.BACKUP_PATH);
        ShellRunner shellRunner = new ShellRunner(serverIP,
                propertyService.getValueByType(serverInfo, Constant.PropertyType.USERNAME),
                propertyService.getValueByType(serverInfo, Constant.PropertyType.PASSWORD));
        List<BackupEntity> backupEntities = new ArrayList<>();
        shellRunner.login();
        // 列出备份目录下所有有备份文件夹
        if (shellRunner.runCommand("ls -F "+backupPath)) {
            List<String> availApps = shellRunner.getResult();
            for (int i = 0; i < availApps.size(); i++) {
                BackupEntity backupEntity = new BackupEntity();
                backupEntity.setName(availApps.get(i).substring(0, availApps.get(i).length()-1));
                // 列出每个应用下所有的可用备份（全部以时间戳作为文件夹名称）
                if (shellRunner.runCommand("ls -F -r "+backupPath+"/"+availApps.get(i)+" | grep -E '*/$'")) {
                    List<String> availBackups = shellRunner.getResult();
                    // 是否存在类似js中的map方法，简化代码？
                    for (int j = 0; j < availBackups.size(); j++)
                        // 会多个斜杠，不影响，为了美观删除掉 [cd|ls /home/deploy] 和 [cd|ls /home//deploy] 效果相同
                        availBackups.set(j, availBackups.get(j).substring(0, availBackups.get(j).length()-1));
                    backupEntity.setList(availBackups);
                }
                backupEntities.add(backupEntity);
            }
            shellRunner.exit();
        } else {
            throw new MyException(Constant.ResultCode.NOT_FOUND, "服务器备份路径下没有应用");
        }
        return new ResponseBuilder().setData(backupEntities).getResponseEntity();
    }

    /**
     * 获取前端仓库列表.
     * */
    @GetMapping("/getRepoList")
    public ResponseEntity  getFrontendRepoList () throws MyException {
        List<Repo> repositoryList = repoService.getRepositoryByType(Constant.PropertyKey.FRONTEND);
        for (int i = 0; i < repositoryList.size(); i++) {
            Repo repo = repositoryList.get(i);
            logger.debug(repo.getRepo()+" "+repo.getFilename()+" "+repo.getLocation());
        }
        ShellRunner localRunner = new ShellRunner(localURL,localUsername,localPassword);
        localRunner.login();
        repositoryList = repoService.getDecoratedRepos(localRunner, repositoryList);
        localRunner.exit();
        logger.info("查找git分支完成");
        return new ResponseBuilder().putItem("repoList",repositoryList).getResponseEntity();
    }

    @GetMapping("/updateRepo")
    public ResponseEntity updateRepo(@NotNull @Param("repoName")String repoName) throws MyException, PackingException {
        Repo repo = repoService.getRepositoryByName(repoName);
        ShellRunner localRunner = new ShellRunner(localURL,localUsername,localPassword);
        localRunner.login();
        if (!buildAppService.isPacking(localRunner, repoName)) {
            uploadService.uploadScript(localRunner,"ListAvailBranch.sh",null);
            List<String> availBranch = repoService.getAvailBranch(localRunner,repo);
            repo.setBranchList(availBranch);
            repo.setDeploy(false);
        } else {
            localRunner.exit();
            throw new PackingException();
        }
        localRunner.runCommand("rm -f ListAvailBranch.sh");
        localRunner.exit();
        return new ResponseBuilder().setData(repo).getResponseEntity();
    }

    /**
     * 获取前端可用脚本.
     * 读取package.json获取npm run可以启动的、以build开始的脚本（忽略start/test等等）
     * @param repo 仓库
     * @param branch 分支。不同分支可用脚本可能不同
     * */
    @GetMapping("/getNpmScripts")
    public ResponseEntity getAvailNpmScripts (String repo,String branch) throws MyException, PackingException {
        Repo _repo = new Repo();
        _repo.setRepo(repo);
        logger.debug(""+repo+" "+branch);
        ShellRunner localRunner = new ShellRunner(localURL,localUsername,localPassword);
        localRunner.login();
        List<String> availNpmScript;
        if (!buildAppService.isPacking(localRunner, repo)) {
            uploadService.uploadScript(localRunner,"GitCheckout.sh","git");
            uploadService.uploadScript(localRunner,"ListAvailScript.sh","frontend");
            availNpmScript = repoService.getAvailNpmScript(localRunner,_repo,branch);
            localRunner.runCommand("rm -f GitCheckout.sh");
            localRunner.runCommand("rm -f ListAvailScript.sh");
            logger.info("已获取全部分支");
        } else {
            localRunner.exit();
            throw new PackingException();
        }
        localRunner.exit();
        return new ResponseBuilder().setData(availNpmScript).getResponseEntity();
    }

    /**
     * 从git部署前端.
     * @param buildDTO 打包信息，包含目标主机和要打的包
     * @return 登录打包机器（本机）无问题后直接返回ok，剩下的打包工作在线程中执行
     * */
    @PostMapping("/deployFromGit")
    public ResponseEntity deployFrontendFromGit(@Valid @RequestBody BuildDTO buildDTO) throws MyException {
        String serverIP = buildDTO.getServerIP();
        BuildEntity[] deployList = buildDTO.getDeployList();
        // 确认要部署的服务器有相应配置，避免白白打包浪费资源
        List<Property> serverInfoList = propertyService.getServerInfo(serverIP);
        if (serverInfoList == null)
            throw new MyException(Constant.ResultCode.NOT_FOUND,"找不到服务器对应配置");
        //登录本地服务器，上传脚本至本地目录(复用，不然还需要写在本地执行脚本的代码。可能会有性能问题)
        ShellRunner localRunner = new ShellRunner(localURL,localUsername,localPassword);
        localRunner.login();
        String frontendPath = userPath+"/"+loginAuth.getUsername()+"/frontend/";
        localRunner.runCommand("rm -r "+frontendPath);
        localRunner.runCommand("mkdir -p "+frontendPath);
        uploadService.uploadScript(localRunner, "BuildFrontend.sh", "frontend");
        buildAppService.buildFrontendThread(localRunner, propertyService.getServerInfo(serverIP), deployList, frontendPath);
        return new ResponseBuilder().getResponseEntity();
    }

    /**
     * 回滚前端.
     * @return 由于回滚比较快，所以不使用线程，执行成功后直接返回
     * */
    @PostMapping("/rollback")
    public ResponseEntity rollbackFrontend(@NotNull @RequestBody RollbackFrontendDTO rollbackDto) throws MyException {
        List<Property> serverInfoList = propertyService.getServerInfo(rollbackDto.getServerIP());
        if (serverInfoList == null)
            throw new MyException(Constant.ResultCode.NOT_FOUND,"找不到服务器对应配置");

        ShellRunner remoteRunner;
        remoteRunner = new ShellRunner(rollbackDto.getServerIP(),
                propertyService.getValueByType(serverInfoList, Constant.PropertyType.USERNAME),
                propertyService.getValueByType(serverInfoList,Constant.PropertyType.PASSWORD));
        remoteRunner.login();
        uploadService.uploadScript(remoteRunner, "RollbackFrontend.sh", "frontend");
        buildAppService.rollbackFrontend(remoteRunner, serverInfoList, rollbackDto.getRollbackData());
        remoteRunner.runCommand("rm -f RollbackFrontend.sh");
        remoteRunner.exit();
        return new ResponseBuilder().getResponseEntity();
    }
}
