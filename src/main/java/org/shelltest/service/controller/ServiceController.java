package org.shelltest.service.controller;

import org.shelltest.service.exception.MyException;
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
    @Value("${local.gitpath}")
    String localGitPath;

    @Autowired
    HttpServletRequest request;

    @PostMapping("/getServerList")
    public String getServerList() {
        logger.info("/service/getServerList");
        return "";
    }

    @PostMapping("/getEurekaList")
    public String getEurekaList(String serverIP) {
        logger.info("/service/getEurekaList");
        return "";
    }

    @PostMapping("/getServiceList")
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
    public String deployServiceFromFile(String serverIP,String phone) {
        logger.info("/service/deployFromFile");
        return "";
    }

    /**
     * 清除jar目录下以往的jar包.
     * */
    @GetMapping("/clearOldJar")
    public ResponseEntity clearOldJar() throws MyException {
        ShellRunner shellRunner = new ShellRunner(localURL, localUsername, localPassword);
        shellRunner.login();
        shellRunner.runCommand("mkdir -p "+localGitPath);
        shellRunner.runCommand("rm -rf "+localGitPath+"/jar/*");
        shellRunner.exit();
        return new ResponseBuilder().getResponseEntity();
    }

    /**
     * 上传文件.
     * 上传jar包
     * @param file 要上传的jar包文件
     * */
    @PostMapping("/uploadServices")
    public ResponseEntity uploadServiceJars(@RequestParam("file") MultipartFile file) {
        logger.info("/service/uploadServices");
        logger.info(file.getOriginalFilename());
        // todo 上传文件
        return new ResponseBuilder().getResponseEntity();
    }


}
