package org.shelltest.service.controller;

import org.jetbrains.annotations.NotNull;
import org.shelltest.service.dto.BuildDTO;
import org.shelltest.service.dto.EurekaDTO;
import org.shelltest.service.entity.Property;
import org.shelltest.service.entity.PropertyExample;
import org.shelltest.service.entity.ServiceArgs;
import org.shelltest.service.exception.MyException;
import org.shelltest.service.mapper.PropertyMapper;
import org.shelltest.service.mapper.ServiceArgsMapper;
import org.shelltest.service.services.*;
import org.shelltest.service.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
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
    StartAppService startAppService;
    @Autowired
    ServiceArgsMapper serviceArgsMapper;

    @GetMapping("/getServerList")
    public ResponseEntity getServerList() {
        logger.info("/service/getServerList");
        List<String> list = propertyService.getValueListByKeys(Constant.PropertyType.IP, Constant.PropertyKey.SERVICE);
        return new ResponseBuilder().putItem("list",list).getResponseEntity();
    }

    @GetMapping("/getEurekaList")
    public ResponseEntity getEurekaList(String serverIP) throws MyException {
        logger.info("/service/getEurekaList");
        List<EurekaDTO> eurekaDTOList = new LinkedList<>();
        String[] services;
        List<Property> serverInfo = propertyService.getServerInfo(serverIP);
        ShellRunner remoteRunner = new ShellRunner(serverIP, propertyService, serverInfo);
        remoteRunner.login();
        if (remoteRunner.runCommand("ls -r "+propertyService.getValueByType(serverInfo, Constant.PropertyType.RUN_PATH)+"|grep -E jar$")) {
            services = remoteRunner.getResultArray();
            logger.info("------获取服务列表------");
            List<ServiceArgs> appNames = serviceArgsMapper.getAppNameListWithDefault(serverIP);
            for (int i = 0; i < services.length; i++) {
                EurekaDTO eurekaDTO = new EurekaDTO();
                eurekaDTO.setJar(services[i]);
                /**
                 * 手动去除jar后缀及时间戳后缀
                 * xxx-[0-9]{4}.jar
                 * 共9位
                 */
                services[i] = services[i].substring(0, services[i].length() - 9);
                for (int j = 0; j < appNames.size(); j++) {
                    if (services[i].equals(appNames.get(j).getFile())) {
                        eurekaDTO.setName(appNames.get(j).getFile());
                        break;
                    }
                }
                eurekaDTO.setPid(startAppService.getProcessPid(remoteRunner, services[i]));
                eurekaDTO.initDataFromProcess(remoteRunner, eurekaDTO.getName());
                eurekaDTOList.add(eurekaDTO);
            }
            logger.info("-----------------------");
        } else {
            throw new MyException(Constant.ResultCode.SHELL_ERROR, "脚本运行错误:"+remoteRunner.getError());
        }
        remoteRunner.exit();
        return new ResponseBuilder().putItem("list", eurekaDTOList).getResponseEntity();
    }

    @GetMapping("/getServiceList")
    public ResponseEntity getServiceList(String serverIP) {
        logger.info("/service/getServiceList");
        return new ResponseBuilder().getResponseEntity();
    }

    @PostMapping("/deployFromGit")
    public ResponseEntity deployServiceFromGit(@RequestBody BuildDTO buildDTO) {
        logger.info("/service/deployFromGit"+(buildDTO==null?" !!empty buildDTO!!":""));
        return new ResponseBuilder().getResponseEntity();
    }

    @GetMapping("/deployServiceFromFile")
    public ResponseEntity deployServiceFromFile(@NotNull @RequestParam("serverIP") String serverIP) throws MyException {
        logger.info("/service/deployFromFile");
        List<Property> serverInfoList = propertyService.getServerInfo(serverIP);
        ShellRunner remoteRunner = new ShellRunner(serverIP, propertyService, serverInfoList);
        remoteRunner.login();
        String username = loginAuth.getUser(request.getHeader(Constant.RequestArg.Auth));
        // 上传脚本，顺便也提前测试下空间有没有满
        uploadService.uploadScript(remoteRunner, "DeployService.sh", "service");
        uploadService.uploadScript(remoteRunner, "StartService.sh", "service");
        buildAppService.deployService(remoteRunner, jarPath+"/"+username,
                propertyService.getValueByType(serverInfoList, Constant.PropertyType.DEPLOY_PATH),
                propertyService.getValueByType(serverInfoList, Constant.PropertyType.BACKUP_PATH),
                propertyService.getValueByType(serverInfoList, Constant.PropertyType.RUN_PATH),
                propertyService.getValueByType(serverInfoList, Constant.PropertyType.LOG_PATH));
        return new ResponseBuilder().getResponseEntity();
    }

    /**
     * 上传文件.
     * 上传jar包
     * @param files 要上传的jar包文件数组
     * */
    @PostMapping("/uploadServices")
    public ResponseEntity uploadServiceJars(@RequestParam("file") MultipartFile[] files) throws MyException {
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
        List<String> prefixList = propertyService.getAppPrefixList();
        List<String> suffixList = propertyService.getAppSuffixList();
        for (int i = 0; i < files.length; i++) {
            File dest = new File(jarPath+"/"+username+"/"+ RenameUtil.getRename(files[i].getOriginalFilename(), prefixList, suffixList)+".jar");
            try {
                logger.debug("重命名文件到："+dest);
                files[i].transferTo(dest);
            } catch (IOException e) {
                // 一般不会发生
                logger.error("后端保存文件失败："+e.getMessage());
                throw new MyException(Constant.ResultCode.FILE_EXCEED, "写文件失败，确认后端服务器有足够内存");
            }
        }
        logger.info("文件已上传至："+jarPath+"/"+username);
        shellRunner.exit();
        logger.info("文件重命名完成");
        logger.info("文件上传结束");
        return new ResponseBuilder().getResponseEntity();
    }
}
