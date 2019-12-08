package org.shelltest.service.controller;

import org.shelltest.service.entity.History;
import org.shelltest.service.exception.MyException;
import org.shelltest.service.mapper.HistoryMapper;
import org.shelltest.service.mapper.ServiceArgsMapper;
import org.shelltest.service.services.BuildAppService;
import org.shelltest.service.services.LoginAuth;
import org.shelltest.service.services.UploadService;
import org.shelltest.service.utils.EncUtil;
import org.shelltest.service.utils.ResponseBuilder;
import org.shelltest.service.utils.ResponseEntity;
import org.shelltest.service.utils.ShellRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
public class TestController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${local.url}")
    String localURL;
    @Value("${local.username}")
    String localUsername;
    @Value("${local.password}")
    String localPassword;
    @Value("${local.path.git}")
    String localGitPath;

    @Autowired
    UploadService uploadService;

    @Autowired
    HistoryMapper historyMapper;

    @Autowired
    BuildAppService buildAppService;
    @Autowired
    ServiceArgsMapper serviceArgsMapper;

    @GetMapping("/test/hello")
    public ResponseEntity sayHello() throws MyException {
        logger.debug("--- hello world sayHello() ---");
        List<String> list = serviceArgsMapper.getArgsWithDefault("192.168.0.2", "eureka");
        return new ResponseBuilder().setData(String.join(" ", list)).getResponseEntity();
    }

    @GetMapping("/backdoor/encode")
    public ResponseEntity getEnc(String pass) {
        return new ResponseBuilder().setData(EncUtil.encode(EncUtil.encode(pass))).getResponseEntity();
    }
    @GetMapping("/backdoor/decode")
    public ResponseEntity getDec(String enc) {
        // .setData(EncUtil.decode(EncUtil.decode(enc)))
        return new ResponseBuilder().getResponseEntity();
    }

    @GetMapping("/test/test")
    public ResponseEntity test() {
        logger.debug("--- hello world test() ---");
        List<History> list = historyMapper.selectNotRead(10);
        for (int i = 0; i < list.size(); i++) {
            logger.debug(list.get(i).getResult());
        }
        return new ResponseBuilder().setData(list).getResponseEntity();
    }

    /**
     * Java执行shell脚本入口
     * @throws Exception
     */
    @GetMapping("/test/run")
    public ResponseEntity service() throws Exception{
        ShellRunner remoteRunner = new ShellRunner("192.168.43.121","server","password");
        remoteRunner.login();

        //打包完成，上传包到远程服务器
//        uploadService.uploadFiles(remoteRunner, "shell",
//                "ms/deploy");
        /*
        //上传完成，在远程服务器进行部署
        uploadService.uploadScript(remoteRunner, "DeployFrontend.sh", "frontend/expect");
        //2.整理部署结果
        if (remoteRunner.runCommand("sh DeployFrontend.sh")) {
            logger.info("部署成功");
        } else {
            logger.error("部署异常："+remoteRunner.getError());
        }*/
        remoteRunner.exit();
        return new ResponseBuilder().getResponseEntity();
    }
}
