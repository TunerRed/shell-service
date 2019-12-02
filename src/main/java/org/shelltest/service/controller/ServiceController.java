package org.shelltest.service.controller;

import org.shelltest.service.entity.Property;
import org.shelltest.service.entity.PropertyExample;
import org.shelltest.service.exception.MyException;
import org.shelltest.service.mapper.PropertyMapper;
import org.shelltest.service.services.BuildAppService;
import org.shelltest.service.services.LoginAuth;
import org.shelltest.service.services.PropertyService;
import org.shelltest.service.services.UploadService;
import org.shelltest.service.utils.Constant;
import org.shelltest.service.utils.ResponseBuilder;
import org.shelltest.service.utils.ResponseEntity;
import org.shelltest.service.utils.ShellRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/service")
public class ServiceController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${local.url}")
    String localURL;
    @Value("${local.username}")
    String localUsername;
    @Value("${local.password}")
    String localPassword;
    @Value("${local.path.git}")
    String localGitPath;
    @Value("${local.path.jar}")
    String jarPath;

    @Autowired
    HttpServletRequest request;
    @Autowired
    LoginAuth loginAuth;
    @Autowired
    UploadService uploadService;
    @Autowired
    PropertyService propertyService;
    @Autowired
    BuildAppService buildAppService;
    @Autowired
    PropertyMapper propertyMapper;

    @GetMapping("/getServerList")
    public ResponseEntity getServerList() {
        logger.info("/service/getServerList");
        PropertyExample example = new PropertyExample();
        example.setDistinct(true);
        example.setOrderByClause("seq");
        example.createCriteria().andTypeEqualTo(Constant.PropertyType.IP)
                .andKeyEqualTo(Constant.PropertyKey.SERVICE);
        List<String> list = propertyMapper.selectValueByExample(example);
        return new ResponseBuilder().putItem("list",list).getResponseEntity();
    }

    @GetMapping("/getEurekaList")
    public String getEurekaList(String serverIP) {
        logger.info("/service/getEurekaList");
        return "";
    }

    @GetMapping("/getServiceList")
    public String getServiceList(String serverIP) {
        logger.info("/service/getServiceList");
        return "";
    }

    @PostMapping("/deployFromGit")
    public String deployServiceFromGit(String serverIP,String phone,String[] deployList) {
        logger.info("/service/deployFromGit");
        return "";
    }

    @PostMapping("/deployServiceFromFile")
    public ResponseEntity deployServiceFromFile(String serverIP) throws MyException {
        logger.info("/service/deployFromFile");
        List<Property> serverInfoList = propertyService.getServerInfo(serverIP);
        ShellRunner remoteRunner = new ShellRunner(serverIP,
                propertyService.getValueByType(serverInfoList, Constant.PropertyType.USERNAME),
                propertyService.getValueByType(serverInfoList, Constant.PropertyType.PASSWORD));
        remoteRunner.login();
        String username = loginAuth.getUser(request.getHeader(Constant.RequestArg.Auth));
        // 上传脚本，顺便也提前测试下空间有没有满
        uploadService.uploadScript(remoteRunner, "DeployService.sh", "service");
        uploadService.uploadScript(remoteRunner, "StartService.sh", "service");
        buildAppService.deployService(remoteRunner, jarPath+"/"+username,
                propertyService.getValueByType(serverInfoList, Constant.PropertyType.DEPLOY_PATH),
                propertyService.getValueByType(serverInfoList, Constant.PropertyType.RUN_PATH));
        return new ResponseBuilder().getResponseEntity();
    }

    /**
     * 上传文件.
     * 上传jar包
     * @param files 要上传的jar包文件数组
     * */
    @PostMapping("/uploadServices")
    public ResponseEntity uploadServiceJars(@RequestParam("file") MultipartFile[] files) throws MyException {
        // todo 文件写入未测试，需要打成jar丢进Linux测试
        logger.info("/service/uploadServices");
        for (int i = 0; i < files.length; i++) {
            if (files[i].isEmpty())
                throw new MyException(Constant.ResultCode.ARGS_ERROR, "莫得文件内容:"+files[i].getOriginalFilename());
        }
        ShellRunner shellRunner = new ShellRunner(localURL, localUsername, localPassword);
        shellRunner.login();
        // 清除jar目录下以往的jar包
        String username = loginAuth.getUser(request.getHeader(Constant.RequestArg.Auth));
        // 用户一对一专属部署文件夹，发生冲突说明你被盗号了
        shellRunner.runCommand("rm -rf "+jarPath+"/"+username);
        shellRunner.runCommand("mkdir -p "+jarPath+"/"+username);
        shellRunner.exit();
        for (int i = 0; i < files.length; i++) {
            File dest = new File(jarPath+"/"+username+"/"+files[i].getOriginalFilename());
            try {
                files[i].transferTo(dest);
            } catch (IOException e) {
                // 一般不会发生
                logger.error("后端保存文件失败："+e.getMessage());
                throw new MyException(Constant.ResultCode.FILE_EXCEED, "写文件失败，确认后端服务器有足够内存");
            }
        }
        logger.info("文件全部上传完成："+jarPath+"/"+username);
        return new ResponseBuilder().getResponseEntity();
    }
}
