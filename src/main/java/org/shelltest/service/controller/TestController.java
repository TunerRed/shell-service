package org.shelltest.service.controller;

import org.shelltest.service.entity.History;
import org.shelltest.service.exception.MyException;
import org.shelltest.service.mapper.HistoryMapper;
import org.shelltest.service.mapper.ServiceArgsMapper;
import org.shelltest.service.services.BuildAppService;
import org.shelltest.service.services.LoginAuth;
import org.shelltest.service.services.StartAppService;
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

import java.util.LinkedList;

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

//    @Value("${local.path.user}")
//    String userPath;
//    @Value("${local.path.user.upload}")
//    String userUploadPath;

    @Autowired
    UploadService uploadService;

    @Autowired
    HistoryMapper historyMapper;

    @Autowired
    BuildAppService buildAppService;
    @Autowired
    ServiceArgsMapper serviceArgsMapper;
    @Autowired
    StartAppService startAppService;

    @GetMapping("/backdoor/encode")
    public ResponseEntity getEnc(String pass) {
        return new ResponseBuilder().setData(EncUtil.encode(pass)).getResponseEntity();
    }
    @GetMapping("/backdoor/decode")
    public ResponseEntity getDec(String enc) {
        // .setData(EncUtil.decode(EncUtil.decode(enc)))
        return new ResponseBuilder().getResponseEntity();
    }

    @GetMapping("/test/hello")
    public ResponseEntity sayHello() throws MyException {
        logger.debug("--- hello world sayHello() ---");
        // List<String> list = serviceArgsMapper.getArgsWithDefault("192.168.0.2", "eureka");
        LinkedList<String> list = new LinkedList<>();
        list.add("test1");
        list.add("test3");
        list.add("test2");
        String[] a = (((LinkedList<String>)list.clone()).toArray(new String[]{}));
        for (int i = 0; i < a.length; i++) {
            logger.debug(a[i]);
        }
        return new ResponseBuilder().setData(String.join(" ", list)).getResponseEntity();
    }

//    @GetMapping("/test/var")
//    public ResponseEntity getPath() {
//        return new ResponseBuilder().putItem("userPath",userPath).putItem("userUploadPath",userUploadPath).getResponseEntity();
//    }

    @GetMapping("/test/startApp")
    public ResponseEntity startApp() throws MyException {
        String serviceArgs =
                String.join(" ", serviceArgsMapper.getArgsWithDefault("192.168.0.2", "eureka"));
        // 启动进程
        // startAppService.startService(null, "a", "eureka", serviceArgs);
        return new ResponseBuilder().getResponseEntity();
    }
}
