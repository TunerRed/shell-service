package org.shelltest.service.controller;

import org.apache.ibatis.annotations.Param;
import org.jetbrains.annotations.NotNull;
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
    PropertyMapper propertyMapper;

    @Value("${local.url}")
    String localURL;
    @Value("${local.username}")
    String localUsername;
    @Value("${local.password}")
    String localPassword;
    @Value("${local.gitpath}")
    String localGitPath;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @PostMapping("/getServerList")
    public ResponseEntity getServerList() {
        PropertyExample example = new PropertyExample();
        example.setDistinct(true);
        example.setOrderByClause("seq");
        example.createCriteria().andTypeEqualTo(Constant.PropertyType.IP)
                .andKeyEqualTo(Constant.PropertyKey.FRONTEND);
        List<String> list = propertyMapper.selectValueByExample(example);
        return new ResponseBuilder().putItem("list",list).getResponseEntity();
    }

    @PostMapping("/getAvailBackup")
    public ResponseEntity  getAvailFrontendBackup(@Param("serverIP")  String serverIP) throws MyException {
        // todo 待测试
        PropertyExample serverExample = new PropertyExample();
        serverExample.createCriteria().andKeyEqualTo(serverIP);
        List<Property> serverInfo = propertyMapper.selectByExample(serverExample);
        String username = propertyService.getPropertyValueByType(serverInfo, Constant.PropertyType.USERNAME);
        String password = propertyService.getPropertyValueByType(serverInfo, Constant.PropertyType.PASSWORD);
        String deployPath = propertyService.getPropertyValueByType(serverInfo, Constant.PropertyType.BACKUP_PATH);
        ShellRunner shellRunner = new ShellRunner(serverIP,username,password);
        List<AvailBackup> availBackups = new ArrayList<>();
        shellRunner.login();
        String shellname = "ListAvailBackup.sh";
        uploadService.uploadScript(shellRunner, shellname,"frontend");
        if (shellRunner.runCommand("sh "+shellname+" "+deployPath)) {
            LinkedList<String> list = shellRunner.getResult();
            int count = list.size();
            for (int i = 0; i < count; i++) {
                availBackups.add(new AvailBackup().setName(list.pop()));
            }
        }else {
            logger.error(shellRunner.getError());
            throw new MyException(Constant.ResultCode.SHELL_ERROR, "脚本执行错误");
        }
        shellRunner.exit();
        return new ResponseBuilder().putItem("list",availBackups).getResponseEntity();
    }

    @PostMapping("/getRepoList")
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
        // localRunner.exit();
        logger.info("查找git分支完成");
        return new ResponseBuilder().putItem("repoList",repositoryList).getResponseEntity();
    }

    @GetMapping("/getNpmScripts")
    public ResponseEntity getAvailNpmScripts (String repo,String branch) throws MyException {
        Repo _repo = new Repo();
        _repo.setRepo(repo);
        logger.debug(""+repo+" "+branch);
        ShellRunner localRunner = new ShellRunner(localURL,localUsername,localPassword);
        localRunner.login();
        uploadService.uploadScript(localRunner,"GitCheckout.sh","git");
        uploadService.uploadScript(localRunner,"ListAvailScript.sh",null);
        List<String> availNpmScript = repoService.getAvailNpmScript(localRunner,_repo,branch);
        localRunner.runCommand("rm -f GitCheckout.sh");
        localRunner.runCommand("rm -f ListAvailScript.sh");
        // localRunner.exit();
        logger.info("已获取全部分支");
        return new ResponseBuilder().setData(availNpmScript).getResponseEntity();
    }

    @PostMapping("/deployFromGit")
    public ResponseEntity deployFrontendFromGit(@Valid @RequestBody BuildDTO buildBundle) throws MyException {
        // todo 封装起来后未测试，要打包的前端若正在打包检测冲突未测试
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
        buildAppService.buildFrontendThread(localRunner, serverIP, deployList);
        return new ResponseBuilder().getResponseEntity();
    }

    @PostMapping("/rollback")
    public ResponseEntity rollbackFrontend(@NotNull @RequestBody RollbackFrontendDTO rollbackDto) throws MyException {
        // todo 回滚未测试
        List<Property> serverInfoList = propertyService.getServerInfo(rollbackDto.getServerIP());
        if (serverInfoList == null)
            throw new MyException(Constant.ResultCode.NOT_FOUND,"找不到服务器对应配置");
        ShellRunner remoteRunner;
        remoteRunner = new ShellRunner(rollbackDto.getServerIP(),
                propertyService.getPropertyValueByType(serverInfoList, Constant.PropertyType.USERNAME),
                propertyService.getPropertyValueByType(serverInfoList,Constant.PropertyType.PASSWORD));
        remoteRunner.login();
        uploadService.uploadScript(remoteRunner, "ListAvailBackup.sh", "frontend");
        buildAppService.rollbackFrontend(remoteRunner, rollbackDto.getServerIP(), rollbackDto.getRollbackData());
        return new ResponseBuilder().getResponseEntity();
    }
}
