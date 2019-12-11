package org.shelltest.service.controller;

import org.apache.ibatis.annotations.Param;
import org.jetbrains.annotations.NotNull;
import org.shelltest.service.dto.BackupEntity;
import org.shelltest.service.dto.BuildEntity;
import org.shelltest.service.dto.RollbackFrontendDTO;
import org.shelltest.service.entity.*;
import org.shelltest.service.exception.MyException;
import org.shelltest.service.mapper.PropertyMapper;
import org.shelltest.service.dto.BuildDTO;
import org.shelltest.service.services.*;
import org.shelltest.service.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import java.util.ArrayList;
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
    PropertyMapper propertyMapper;

    @Value("${local.url}")
    String localURL;
    @Value("${local.username}")
    String localUsername;
    @Value("${local.password}")
    String localPassword;
    @Value("${local.path.git}")
    String localGitPath;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 获取所有前端服务器列表.
     * 是否要对每个用户做可打包权限的控制？目前没有该想法（不难做，不想做）
     * 一个ip只能有一套配置，即目前的架构无法完成同主机多账户部署
     * */
    @GetMapping("/getServerList")
    public ResponseEntity getServerList() {
        PropertyExample example = new PropertyExample();
        example.setDistinct(true);
        example.setOrderByClause("seq");
        example.createCriteria().andTypeEqualTo(Constant.PropertyType.IP)
                .andKeyEqualTo(Constant.PropertyKey.FRONTEND);
        List<String> list = propertyMapper.selectValueByExample(example);
        return new ResponseBuilder().putItem("list",list).getResponseEntity();
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
        ShellRunner localRunner = new ShellRunner(localURL,localUsername,localPassword);
        localRunner.login();
        List<Repo> repositoryList = repoService.getRepositoryByType(Constant.PropertyKey.FRONTEND);
        uploadService.uploadScript(localRunner,"ListAvailBranch.sh",null);
        for (int i = 0; i < repositoryList.size(); i++) {
            logger.info("查找项目可用git分支："+repositoryList.get(i).getRepo());
            if (!buildAppService.isPacking(localRunner, repositoryList.get(i).getRepo())) {
                List<String> availBranch = repoService.getAvailBranch(localRunner,repositoryList.get(i));
                repositoryList.get(i).setBranchList(availBranch);
            } else {
                repositoryList.get(i).setBranchList(null);
            }
            repositoryList.get(i).setDeploy(false);
        }
        localRunner.runCommand("rm -f ListAvailBranch.sh");
        localRunner.exit();
        logger.info("查找git分支完成");
        return new ResponseBuilder().putItem("repoList",repositoryList).getResponseEntity();
    }

    /**
     * 获取前端可用脚本.
     * 读取package.json获取npm run可以启动的、以build开始的脚本（忽略start/test等等）
     * @param repo 仓库
     * @param branch 分支。不同分支可用脚本可能不同
     * */
    @GetMapping("/getNpmScripts")
    public ResponseEntity getAvailNpmScripts (String repo,String branch) throws MyException {
        Repo _repo = new Repo();
        _repo.setRepo(repo);
        logger.debug(""+repo+" "+branch);
        ShellRunner localRunner = new ShellRunner(localURL,localUsername,localPassword);
        localRunner.login();
        uploadService.uploadScript(localRunner,"GitCheckout.sh","git");
        uploadService.uploadScript(localRunner,"ListAvailScript.sh","frontend");
        List<String> availNpmScript = repoService.getAvailNpmScript(localRunner,_repo,branch);
        localRunner.runCommand("rm -f GitCheckout.sh");
        localRunner.runCommand("rm -f ListAvailScript.sh");
        localRunner.exit();
        logger.info("已获取全部分支");
        return new ResponseBuilder().setData(availNpmScript).getResponseEntity();
    }

    /**
     * 从git部署前端.
     * @param buildBundle 打包信息，包含目标主机和要打的包
     * @return 登录打包机器（本机）无问题后直接返回ok，剩下的打包工作在线程中执行
     * */
    @PostMapping("/deployFromGit")
    public ResponseEntity deployFrontendFromGit(@Valid @RequestBody BuildDTO buildBundle) throws MyException {
        // todo @Valid
        // 数据包DTO 原本不知道规范名称，所以起名为Bundle
        String serverIP = buildBundle.getServerIP();
        BuildEntity[] deployList = buildBundle.getDeployList();
        // 确认要部署的服务器有相应配置，避免白白打包浪费资源
        List<Property> serverInfoList = propertyService.getServerInfo(serverIP);
        if (serverInfoList == null)
            throw new MyException(Constant.ResultCode.NOT_FOUND,"找不到服务器对应配置");
        //登录本地服务器，上传脚本至本地目录(复用，不然还需要些在本地执行脚本的代码。可能会有性能问题)
        ShellRunner localRunner = new ShellRunner(localURL,localUsername,localPassword);
        localRunner.login();
        localRunner.runCommand("rm -r "+localGitPath+"/*.tar.gz");
        uploadService.uploadScript(localRunner, "BuildFrontend.sh", "frontend");
        // uploadService.uploadScript(localRunner, "LoginAuth.sh", "frontend/expect");
        buildAppService.buildFrontendThread(localRunner, propertyService.getServerInfo(serverIP), deployList);
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
