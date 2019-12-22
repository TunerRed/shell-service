package org.shelltest.service.controller;

import org.apache.ibatis.annotations.Param;
import org.jetbrains.annotations.NotNull;
import org.shelltest.service.dto.BuildDTO;
import org.shelltest.service.dto.EurekaDTO;
import org.shelltest.service.entity.History;
import org.shelltest.service.entity.Property;
import org.shelltest.service.entity.PropertyExample;
import org.shelltest.service.entity.ServiceArgs;
import org.shelltest.service.exception.MockException;
import org.shelltest.service.exception.MyException;
import org.shelltest.service.mapper.HistoryMapper;
import org.shelltest.service.mapper.PropertyMapper;
import org.shelltest.service.mapper.ServiceArgsMapper;
import org.shelltest.service.services.*;
import org.shelltest.service.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;
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
    DeployLogUtil deployUtil;
    @Autowired
    OtherUtil otherUtil;

    @Autowired
    ServiceArgsMapper serviceArgsMapper;
    @Autowired
    HistoryMapper historyMapper;

    @GetMapping("/getServerList")
    public ResponseEntity getServerList() {
        logger.info("/service/getServerList");
        List<String> authServers = otherUtil.getGrantedServerList(Constant.PropertyKey.SERVICE);
        return new ResponseBuilder().putItem("list",authServers).getResponseEntity();
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

    @GetMapping("/stop")
    public ResponseEntity stopService(@NotNull@Param("serverIP")String serverIP,
                                      @NotNull@Param("name")String filename, @NotNull@Param("pid")int pid) throws MyException {
        logger.info("杀进程：/service/stop");
        ShellRunner remoteRunner = new ShellRunner(serverIP, propertyService);
        remoteRunner.login();
        int runningPid = startAppService.getProcessPid(remoteRunner, filename);
        if (runningPid == pid && startAppService.killService(remoteRunner, filename))
            return new ResponseBuilder().getResponseEntity();
        throw new MyException(Constant.ResultCode.ARGS_ERROR, "未找到指定进程或杀进程失败\n文件【"+filename
                +"】 指定的pid:"+pid+" 查询到的pid:"+runningPid+"\n"+remoteRunner.getError());
    }

    @GetMapping("/start")
    public ResponseEntity startService(@NotNull@Param("serverIP")String serverIP, @NotNull@Param("name")String filename) throws MyException {
        logger.info("启动进程：/service/start");
        // todo 起进程记录在history中不合适
        List<Property> serverInfo = propertyService.getServerInfo(serverIP);
        String serviceArgs =
                String.join(" ", serviceArgsMapper.getArgsWithDefault(serverIP, filename));
        ShellRunner remoteRunner = new ShellRunner(serverIP, propertyService, serverInfo);
        remoteRunner.login();
        History deployLog = deployUtil.createLogEntity(serverIP);
        StringBuffer result = new StringBuffer();
        result.append("类型：启动服务\n-------------------\n");
        uploadService.uploadScript(remoteRunner, "StartService.sh", "service");
        if (startAppService.killService(remoteRunner, filename))
            result.append("杀进程\n");
        new Thread(()->{
            try {
                if (startAppService.startService(remoteRunner, filename,
                        propertyService.getValueByType(serverInfo, Constant.PropertyType.RUN_PATH), serviceArgs,
                        propertyService.getValueByType(serverInfo, Constant.PropertyType.LOG_PATH))) {
                    result.append("启动成功:"+remoteRunner.getResult().toString()+"\n");
                } else {
                    result.append("启动失败\n");
                }
                result.append("错误信息："+remoteRunner.getError());
            } catch (MyException e) {
                result.append("启动异常："+e.getMessage());
                logger.error(e.getMessage());
            } catch (Exception e) {
                result.append("意外的异常"+e.getMessage());
                e.printStackTrace();
            }
            result.append("\n---------------\n");
            deployLog.setResult(result.toString());
            deployLog.setEndTime(new Date());
            historyMapper.insertSelective(deployLog);
            logger.info("----- 服务启动完成，已存储记录 -----");
        }).start();
        return new ResponseBuilder().getResponseEntity();
    }

    @GetMapping("/getServiceList")
    public ResponseEntity getServiceList(String serverIP) {
        logger.info("/service/getServiceList");
        return new ResponseBuilder().getResponseEntity();
    }

    /**
     * 下载服务包.
     * @param serverIP 目标主句
     * @param filename 运行路径下的文件
     * @return 下载文件
     * */
    @GetMapping("/download")
    public org.springframework.http.ResponseEntity downloadService(@NotNull@Param("serverIP")String serverIP, @NotNull@Param("filename")String filename) throws MyException {
        logger.info("文件下载:/service/download");
        Long len = null;
        InputStreamResource res = null;
        HttpHeaders headers = new HttpHeaders();
        filename = filename.substring(filename.lastIndexOf('/') + 1);
        List<Property> serverInfo = propertyService.getServerInfo(serverIP);

        // 清空本地的下载目录
        ShellRunner localRunner = new ShellRunner(localURL, localUsername, localPassword);
        localRunner.login();
        localRunner.runCommand("rm -rf "+jarPath+"/download/*");
        localRunner.exit();

        ShellRunner remoteRunner = new ShellRunner(serverIP, propertyService, serverInfo);
        remoteRunner.login();
        uploadService.downloadFile(remoteRunner, jarPath+"/download",
                propertyService.getValueByType(serverInfo, Constant.PropertyType.RUN_PATH), filename);
        File file = new File(jarPath+"/download/"+filename);
        if (!file.exists() || !file.canRead()) {
            remoteRunner.exit();
            throw new MyException(Constant.ResultCode.NOT_FOUND, "不可读取的本地文件");
        } else {
            try {
                headers.add("Cache-Control","no-cache,no-store,must-revalidate");
                headers.add("Pragma", "no-cache");
                headers.add("Expires", "0");
                headers.add("Content-Disposition", "attachment;filename=" + URLEncoder.encode(file.getName(), "UTF-8"));
                FileSystemResource f = new FileSystemResource(file);
                res = new InputStreamResource(f.getInputStream());
                len = f.contentLength();
            } catch (UnsupportedEncodingException e) {
                logger.error("文件名编码转换错误 "+e.getMessage());
                throw new MyException(Constant.ResultCode.ARGS_ERROR, "文件名编码转换错误 "+e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                throw new MyException(Constant.ResultCode.INTERNAL_ERROR, "文件下载错误 "+e.getMessage());
            } finally {
                remoteRunner.exit();
            }
        }
        return org.springframework.http.ResponseEntity.ok().headers(headers).contentLength(len)
                .contentType(MediaType.APPLICATION_OCTET_STREAM).body(res);
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
                throw new MyException(Constant.ResultCode.FILE_ERROR, "莫得文件内容:"+files[i].getOriginalFilename());
        }
        List<String> nameList = new LinkedList<>();
        try {
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
                String filename = otherUtil.getRename(files[i].getOriginalFilename(), prefixList, suffixList)+".jar";
                File dest = new File(jarPath+"/"+username+"/"+ filename);
                try {
                    logger.debug("重命名文件到："+dest);
                    files[i].transferTo(dest);
                    nameList.add(filename);
                } catch (IOException e) {
                    // 一般不会发生
                    logger.error("后端保存文件失败："+e.getMessage());
                    throw new MyException(Constant.ResultCode.FILE_ERROR, "写文件失败，确认后端服务器有足够内存");
                }
            }
            logger.info("文件已上传至："+jarPath+"/"+username);
            shellRunner.exit();
            logger.info("文件重命名完成");
            logger.info("文件上传结束");
        } catch (MyException e) {
            e.setResultCode(Constant.ResultCode.FILE_ERROR);
            throw e;
        }
        return new ResponseBuilder().setData(nameList).getResponseEntity();
    }
}
