package org.shelltest.service.controller;

import org.apache.ibatis.annotations.Param;
import org.jetbrains.annotations.NotNull;
import org.shelltest.service.dto.AppStartInDTO;
import org.shelltest.service.dto.BuildDTO;
import org.shelltest.service.dto.BuildEntity;
import org.shelltest.service.dto.EurekaDTO;
import org.shelltest.service.entity.*;
import org.shelltest.service.exception.MockException;
import org.shelltest.service.exception.MyException;
import org.shelltest.service.exception.PackingException;
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
    RepoService repoService;

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
        List<String> authServers = otherUtil.getGrantedServerList(Constant.PropertyKey.SERVICE);
        return new ResponseBuilder().putItem("list",authServers).getResponseEntity();
    }

    @GetMapping("/getEurekaList")
    public ResponseEntity getEurekaList(String serverIP) throws MyException {
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
        ShellRunner remoteRunner = new ShellRunner(serverIP, propertyService);
        remoteRunner.login();
        int runningPid = startAppService.getProcessPid(remoteRunner, filename);
        if (runningPid == pid && startAppService.killService(remoteRunner, filename))
            return new ResponseBuilder().getResponseEntity();
        throw new MyException(Constant.ResultCode.ARGS_ERROR, "未找到指定进程或杀进程失败\n文件【"+filename
                +"】 指定的pid:"+pid+" 查询到的pid:"+runningPid+"\n"+remoteRunner.getError());
    }

    @PostMapping("/start")
    public ResponseEntity startService(@RequestBody AppStartInDTO startInDTO) throws MyException {
        // todo 起进程记录在history中不合适
        logger.debug(startInDTO.getServerIP());
        String serverIP = startInDTO.getServerIP();
        List<String> filenames = startInDTO.getFilenames();
        List<Property> serverInfo = propertyService.getServerInfo(serverIP);
        for (int i = 0; i < filenames.size(); i++) {
                String filename = filenames.get(i);
                logger.debug(filename);
        }
        ShellRunner remoteRunner = new ShellRunner(serverIP, propertyService, serverInfo);
        remoteRunner.login();
        History deployLog = deployUtil.createLogEntity(serverIP);
        StringBuffer result = new StringBuffer();
        result.append("类型：启动服务\n-------------------\n");
        uploadService.uploadScript(remoteRunner, "StartService.sh", "service");

        new Thread(()->{
            try {
                for (int i = 0; i < filenames.size(); i++) {
                    String filename = filenames.get(i);
                    logger.debug(filename);
                    if (startAppService.killService(remoteRunner, filename))
                        result.append("杀进程\n");
                    String serviceArgs =
                            String.join(" ", serviceArgsMapper.getArgsWithDefault(serverIP, filename));
                    if (startAppService.startService(remoteRunner, filename,
                            propertyService.getValueByType(serverInfo, Constant.PropertyType.RUN_PATH), serviceArgs,
                            propertyService.getValueByType(serverInfo, Constant.PropertyType.LOG_PATH))) {
                        result.append("启动成功:"+remoteRunner.getResult().toString()+"\n");
                    } else {
                        result.append("启动失败\n");
                    }
                    result.append("错误信息："+remoteRunner.getError());
                }
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

    @GetMapping("/getRepoList")
    public ResponseEntity getServiceList() throws MyException {
        List<Repo> repositoryList = repoService.getRepositoryByType(Constant.PropertyKey.SERVICE);
        ShellRunner localRunner = new ShellRunner(localURL,localUsername,localPassword);
        localRunner.login();
        repositoryList = repoService.getDecoratedRepos(localRunner, repositoryList);
        localRunner.exit();
        logger.info("查找git分支完成");
        return new ResponseBuilder().putItem("repoList",repositoryList).getResponseEntity();
    }

    /**
     * 下载服务包.
     * @param serverIP 目标主句
     * @param filename 运行路径下的文件
     * @return 下载文件
     * */
    @GetMapping("/download")
    public org.springframework.http.ResponseEntity downloadService(@NotNull@Param("serverIP")String serverIP, @NotNull@Param("filename")String filename) throws MyException {
        Long len = null;
        InputStreamResource res = null;
        HttpHeaders headers = new HttpHeaders();
        filename = filename.substring(filename.lastIndexOf('/') + 1);
        List<Property> serverInfo = propertyService.getServerInfo(serverIP);

        // 新建本地下载目录，因为可能会同时下载多个文件，所以不删除
        // todo 要有一个策略去清理这些文件
        ShellRunner localRunner = new ShellRunner(localURL, localUsername, localPassword);
        localRunner.login();
        String downloadPath = loginAuth.getUserResourcePath("download");
        localRunner.runCommand("mkdir -p "+downloadPath);
        localRunner.exit();

        ShellRunner remoteRunner = new ShellRunner(serverIP, propertyService, serverInfo);
        remoteRunner.login();
        uploadService.downloadFile(remoteRunner, downloadPath,
                propertyService.getValueByType(serverInfo, Constant.PropertyType.RUN_PATH), filename);
        File file = new File(downloadPath+filename);
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

    @PostMapping("/deployFromGit")
    public ResponseEntity deployServiceFromGit(@RequestBody BuildDTO buildDTO) throws MyException {
        String serverIP = buildDTO.getServerIP();
        BuildEntity[] deployList = buildDTO.getDeployList();
        // 确认要部署的服务器有相应配置，避免白白打包浪费资源
        List<Property> serverInfoList = propertyService.getServerInfo(serverIP);
        if (serverInfoList == null)
            throw new MyException(Constant.ResultCode.NOT_FOUND,"找不到服务器对应配置");
        //登录本地服务器，上传脚本至本地目录(复用，不然还需要写在本地执行脚本的代码。可能会有性能问题)
        ShellRunner localRunner = new ShellRunner(localURL,localUsername,localPassword);
        localRunner.login();
        String servicePath = loginAuth.getUserResourcePath("service");
        localRunner.runCommand("rm -r "+servicePath);
        localRunner.runCommand("mkdir -p "+servicePath);
        uploadService.uploadScript(localRunner, "BuildService.sh", "service");
        buildAppService.buildServiceThread(localRunner, propertyService.getServerInfo(serverIP), deployList, servicePath);
        return new ResponseBuilder().getResponseEntity();
    }

    @GetMapping("/deployServiceFromFile")
    public ResponseEntity deployServiceFromFile(@NotNull @RequestParam("serverIP") String serverIP) throws MyException {
        List<Property> serverInfoList = propertyService.getServerInfo(serverIP);
        ShellRunner remoteRunner = new ShellRunner(serverIP, propertyService, serverInfoList);
        remoteRunner.login();
        // 上传脚本，顺便也提前测试下空间有没有满
        uploadService.uploadScript(remoteRunner, "DeployService.sh", "service");
        uploadService.uploadScript(remoteRunner, "StartService.sh", "service");
        buildAppService.deployService(remoteRunner, loginAuth.getUserResourcePath("upload"),
                propertyService.getValueByType(serverInfoList, Constant.PropertyType.DEPLOY_PATH),
                propertyService.getValueByType(serverInfoList, Constant.PropertyType.BACKUP_PATH),
                propertyService.getValueByType(serverInfoList, Constant.PropertyType.RUN_PATH),
                propertyService.getValueByType(serverInfoList, Constant.PropertyType.LOG_PATH),
                deployUtil.createLogEntity(remoteRunner));
        return new ResponseBuilder().getResponseEntity();
    }

    @GetMapping("/clearUpload")
    public ResponseEntity clearUploadDir() throws MyException {
        ShellRunner shellRunner = new ShellRunner(localURL, localUsername, localPassword);
        shellRunner.login();
        // 清除jar目录下以往的jar包
        String uploadPath = loginAuth.getUserResourcePath("upload");
        shellRunner.runCommand("rm -r "+uploadPath);
        shellRunner.runCommand("mkdir -p "+uploadPath);
        shellRunner.exit();
        logger.info("清理旧文件完成");
        return new ResponseBuilder().getResponseEntity();
    }

    /**
     * 上传文件.
     * 上传jar包
     * @param file 要上传的jar文件
     * */
    @PostMapping("/uploadService")
    public ResponseEntity uploadServiceJar(@RequestParam("file") MultipartFile file) throws MyException {
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".jar"))
            throw new MyException(Constant.ResultCode.FILE_ERROR, "文件错误:"+file.getOriginalFilename());
        String filename = "";
        try {
            // 清除jar目录下以往的jar包
            String uploadPath = loginAuth.getUserResourcePath("upload");
            List<String> prefixList = propertyService.getAppPrefixList();
            List<String> suffixList = propertyService.getAppSuffixList();
            filename = otherUtil.getRename(file.getOriginalFilename(), prefixList, suffixList)+".jar";
            File dest = new File(uploadPath+ filename);
            try {
                logger.debug("重命名文件到："+dest);
                dest.mkdirs();
                file.transferTo(dest);
                logger.info("文件已上传至："+uploadPath);
            } catch (IOException e) {
                // 一般不会发生
                logger.error("后端保存文件失败："+e.getMessage());
                throw new MyException(Constant.ResultCode.FILE_ERROR, "写文件失败，确认后端服务器有足够内存");
            }
            logger.info("文件上传结束");
        } catch (MyException e) {
            e.setResultCode(Constant.ResultCode.FILE_ERROR);
            throw e;
        }
        return new ResponseBuilder().setData(filename).getResponseEntity();
    }

    /**
     * 已弃用的.
     * 上传jar包
     * @param files 要上传的jar包文件数组
     * */
    @PostMapping("/uploadServices")
    public ResponseEntity uploadServiceJars(@RequestParam("file") MultipartFile[] files) throws MyException {
        for (int i = 0; i < files.length; i++) {
            if (files[i].isEmpty())
                throw new MyException(Constant.ResultCode.FILE_ERROR, "莫得文件内容:"+files[i].getOriginalFilename());
        }
        List<String> nameList = new LinkedList<>();
        try {
            ShellRunner shellRunner = new ShellRunner(localURL, localUsername, localPassword);
            shellRunner.login();
            // 清除jar目录下以往的jar包
            String path = loginAuth.getUserResourcePath("upload");
            shellRunner.runCommand("mkdir -p " + path);
            List<String> prefixList = propertyService.getAppPrefixList();
            List<String> suffixList = propertyService.getAppSuffixList();
            for (int i = 0; i < files.length; i++) {
                String filename = otherUtil.getRename(files[i].getOriginalFilename(), prefixList, suffixList)+".jar";
                File dest = new File(path + filename);
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
            logger.info("文件已上传至："+path);
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
