package org.shelltest.service.services;

import org.jetbrains.annotations.NotNull;
import org.shelltest.service.dto.RollbackFrontendEntity;
import org.shelltest.service.dto.BuildEntity;
import org.shelltest.service.entity.History;
import org.shelltest.service.entity.Property;
import org.shelltest.service.exception.MyException;
import org.shelltest.service.mapper.HistoryMapper;
import org.shelltest.service.mapper.ServiceArgsMapper;
import org.shelltest.service.utils.Constant;
import org.shelltest.service.utils.DeployLogUtil;
import org.shelltest.service.utils.OtherUtil;
import org.shelltest.service.utils.ShellRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class BuildAppService {

    @Autowired
    UploadService uploadService;
    @Autowired
    PropertyService propertyService;
    @Autowired
    StartAppService startAppService;
    @Autowired
    LoginAuth loginAuth;

    @Autowired
    HistoryMapper historyMapper;
    @Autowired
    ServiceArgsMapper serviceArgsMapper;
    @Autowired
    DeployLogUtil deployUtil;
    @Autowired
    OtherUtil otherUtil;

    @Value("${local.path.git}")
    String localGitPath;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 部署微服务.
     * @param remoteRunner 远程连接
     * @param localPath jar包在本机的路径
     * @param deployPath 要上传到远程路径
     * @param runPath 远程机器上jar包运行的路径
     * */
    public void deployService(ShellRunner remoteRunner, String localPath, String deployPath,
                              String backupPath,String runPath,String logPath, History deployLog) {
        StringBuffer deployResult = new StringBuffer();
        deployResult.append("类型：部署后端\n");
        deployResult.append("服务器："+deployLog.getTarget()+"\n");
        new Thread(()->{
            try {
                /**
                 * 上传文件，并获取文件列表
                 * 适用情况：所有jar包都在同一目录下.
                 * 当各有目录时，应当使用for循环依次上传，数据库表也会有改动
                 * 同时因为多次调用scp，性能可能也会下降
                 * 此处默认所有jar都在同一目录下，视各自情况改动代码、添加path字段等等
                 * */
                String[] services = uploadService.uploadFiles(remoteRunner, localPath, deployPath, "jar");
                deployResult.append("上传至远程服务器成功\n");
                if (remoteRunner.runCommand("sh DeployService.sh"+ShellRunner.appendArgs(new String[]{deployPath, backupPath, runPath}))) {
                    deployResult.append("部署/替换jar包成功\n---------------------------\n");
                    /**
                     * 系统架构：eureka + config（从数据库获取其他应用的配置） + 其他需要获取各种配置的应用
                     * config放第二个，eureka放第一个，若第一个不是eureka，第一第二交换
                     * */

                /*
                for (int i = 1; i < services.length; i++) {
                    if (services[i].contains("config"))
                        swap(services, 1, i);
                    else if (services[i].contains("eureka"))
                        swap(services, 0, i);
                }
                if (services.length > 1 && !services[0].contains("eureka"))
                    swap(services, 0, 1);
                */

                    for (int i = 2; i < services.length; i++)
                        if (services[i].contains("config")) {
                            swap(services, 1, i);
                            break;
                        }
                    for (int i = 1; i < services.length; i++)
                        if (services[i].contains("eureka")) {
                            swap(services, 0, i);
                            break;
                        }
                    if (services.length > 1 && !services[0].contains("eureka"))
                        swap(services, 0, 1);

                    for (int i = 0; i < services.length; i++) {
                        services[i] = services[i].substring(0, services[i].lastIndexOf(".jar"));
                        // 可行性前提：多次使用-D指定同一个属性，以最后指定的为准
                        String serviceArgs =
                                String.join(" ", serviceArgsMapper.getArgsWithDefault(deployLog.getTarget(), services[i]));
                        // 启动进程
                        deployResult.append("服务启动信息："+services[i]+"\n");
                        try {
                            if (startAppService.killService(remoteRunner, services[i]))
                                deployResult.append("杀旧进程："+services[i]+"\n");
                            if (startAppService.startService(remoteRunner, services[i], runPath, serviceArgs, logPath)) {
                                deployResult.append("【成功】"+remoteRunner.getResult().toString()+"\n");
                            } else {
                                deployResult.append("【失败】\n");
                            }
                            if (remoteRunner.getError().length() > 0)
                                deployResult.append("错误信息：\n"+remoteRunner.getError());
                            logger.info("服务启动完成"+services[i]);
                        } catch (MyException e) {
                            deployResult.append("服务启动异常："+e.getMessage());
                            e.printStackTrace();
                        }
                        deployResult.append("---------------------------\n");
                    }
                } else {
                    deployResult.append("上传至远程服务器异常\n");
                }
            } catch (MyException e) {
                deployResult.append("部署错误："+e.getMessage()+"\n");
                logger.error(e.getMessage());
            } catch (Exception e) {
                deployResult.append("意外的Exception"+e.getMessage()+"\n");
                e.printStackTrace();
            } finally {
                try {
                    remoteRunner.runCommand("rm -f DeployService.sh");
                    remoteRunner.runCommand("rm -f StartService.sh");
                    remoteRunner.exit();
                } catch (MyException e){e.printStackTrace();}
                //3.全工程clear，写日志到数据库
                deployLog.setEndTime(new Date());
                deployLog.setResult(deployResult.toString());
                historyMapper.insertSelective(deployLog);
                logger.info("--- 部署完成，已存储记录 ---");
            }
        }).start();
    }

    public void buildServiceThread(ShellRunner localRunner, List<Property> serverInfoList,  BuildEntity[] deployList, String servicePath) throws MyException {
        // 记录犯罪证据.jpg(所有信息的key都是目标主机的ip)
        History packageLog = deployUtil.createLogEntity(serverInfoList.get(0).getKey());
        History deployLog = deployUtil.createLogEntity(serverInfoList.get(0).getKey());

        ShellRunner remoteUploader = new ShellRunner(packageLog.getTarget(), propertyService.getValueByType(serverInfoList, Constant.PropertyType.USERNAME),
                propertyService.getValueByType(serverInfoList,Constant.PropertyType.PASSWORD));
        remoteUploader.login();
        // 提前上传脚本，测试下空间有没有满
        uploadService.uploadScript(remoteUploader, "DeployService.sh", "service");
        uploadService.uploadScript(remoteUploader, "StartService.sh", "service");
        remoteUploader.exit();

        // 开始打包
        new Thread(()->{
            StringBuffer deployResult = new StringBuffer();
            try {
                logger.info("--- 处理打包请求 ---");
                deployResult.append("类型：从Git打包后端\n");
                deployResult.append("服务器："+packageLog.getTarget()+"\n");
                deployResult.append("----------------\n");
                //在本机运行shell进行打包
                for (int i = 0; i < deployList.length; i++) {
                    BuildEntity buildEntity = deployList[i];
                    String basicInfo = deployList[i].getRepo()+"/";
                    basicInfo += deployList[i].getBranch()+"/[";
                    basicInfo += deployList[i].getLocation()+"]\n";
                    deployResult.append(basicInfo);
                    String buildInfo = buildService(localRunner, buildEntity.getRepo(), buildEntity.getBranch(), buildEntity.isDeploy(),
                            buildEntity.getLocation(), servicePath);
                    deployResult.append("打包结果：" + buildInfo + "\n");
                }
                //打包完成，不需要在本地再执行shell，退出这层远程登录，并登录远程和服务器
                localRunner.runCommand("rm -f BuildService.sh");

                // 对打包后的应用进行重命名
                List<String> prefixList = propertyService.getAppPrefixList();
                List<String> suffixList = propertyService.getAppSuffixList();
                if (localRunner.runCommand("ls "+servicePath+"|egrep *.jar$")) {
                    List<String> services = localRunner.getResult();
                    if (services==null || services.size() == 0) {
                        deployResult.append("未发现打包成功的应用!");
                        localRunner.exit();
                        return;
                    } else {
                        for (int i = 0; i < services.size(); i++) {
                            String filename = services.get(i);
                            String rename = otherUtil.getRename(filename, prefixList, suffixList);
                            localRunner.runCommand("mv "+servicePath+"/"+filename+" "+servicePath+"/"+rename+".jar");
                        }
                    }
                }
                // 退出登录
                localRunner.exit();
            } catch (MyException e) {
                logger.error(e.getMessage());
                deployResult.append("打包错误！\n"+e.getMessage());
            } finally {
                // 打包完成，写日志到数据库
                packageLog.setEndTime(new Date());
                packageLog.setResult(deployResult.toString());
                historyMapper.insertSelective(packageLog);
                logger.info("--- 打包完成，已存储记录 ---");
            }
            // 按照从文件部署的思路进行部署，上传脚本后执行部署方法，最后存储记录
            try {
                ShellRunner startService = new ShellRunner(deployLog.getTarget(), propertyService.getValueByType(serverInfoList, Constant.PropertyType.USERNAME),
                        propertyService.getValueByType(serverInfoList,Constant.PropertyType.PASSWORD));
                startService.login();
                deployService(startService, servicePath,
                        propertyService.getValueByType(serverInfoList, Constant.PropertyType.DEPLOY_PATH),
                        propertyService.getValueByType(serverInfoList, Constant.PropertyType.BACKUP_PATH),
                        propertyService.getValueByType(serverInfoList, Constant.PropertyType.RUN_PATH),
                        propertyService.getValueByType(serverInfoList, Constant.PropertyType.LOG_PATH),
                        deployLog);
            } catch (MyException e) {
                logger.error(e.getMessage());
            }
        }).start();
    }

    /**
     * 打包单个后端.
     * @param shellRunner 本地连接
     * @param gitRepository 要打包的git仓库
     * @param gitBranch 要checkout到的git分支
     * @param isDeploy mvn install后是否用作部署（即是否只作为依赖包）
     * @param jarPath 生成的jar包在仓库内的相对位置
     * @param targetPath 打包后的jar要移动到的文件夹
     * @return 打包信息，是否打包成功
     * */
    public String buildService(@NotNull ShellRunner shellRunner, String gitRepository, String gitBranch, boolean isDeploy, String jarPath, String targetPath) throws MyException {
        logger.debug("后端："+gitRepository+" 打包中");
        if (!isPacking(shellRunner, gitRepository)) {
            shellRunner.runCommand("sh BuildService.sh"
                    + ShellRunner.appendArgs(new String[]{localGitPath,gitRepository,gitBranch,jarPath,targetPath,isDeploy?"1":"0"}),null);
            logger.debug("后端："+gitRepository+" 打包完成");
            if (shellRunner.isSuccess())
                return "打包成功\n"+shellRunner.getError();
            else
                return "打包异常\n"+shellRunner.getError();
        } else {
            return "打包失败:"+gitRepository+"正在打包，请稍后重试";
        }
    }

    /**
     * 打包前端列表.
     * 因为打包时间过长，故使用线程
     * 后台运行线程的方法不应该抛异常，抛出来给谁看？
     * @param localRunner 本机连接
     * @param serverInfoList 要部署的服务器的配置列表
     * @param deployList 要部署的应用列表
     */
    public void buildFrontendThread(ShellRunner localRunner, List<Property> serverInfoList,  BuildEntity[] deployList, String frontendPath) {
        // 记录犯罪证据.jpg(所有信息的key都是目标主机的ip)
        History deployLog = deployUtil.createLogEntity(serverInfoList.get(0).getKey());
        new Thread(()->{
            StringBuffer deployResult = new StringBuffer();
            ShellRunner remoteRunner = null;
            try {
                logger.info("--- 处理打包请求 ---");
                deployResult.append("类型：从Git部署前端\n");
                deployResult.append("服务器："+deployLog.getTarget()+"\n");
                deployResult.append("----------------\n");
                //在本机运行shell进行打包
                for (int i = 0; i < deployList.length; i++) {
                    String basicInfo = deployList[i].getRepo()+"/";
                    basicInfo += deployList[i].getBranch()+"/";
                    basicInfo += deployList[i].getScript()+"/";
                    basicInfo += deployList[i].getFilename()+"\n";
                    deployResult.append(basicInfo);
                    String buildInfo = buildFrontend(localRunner, deployList[i].getRepo(), deployList[i].getBranch(),
                            deployList[i].getScript(), frontendPath, deployList[i].getFilename());
                    deployResult.append("结果：" + buildInfo + "\n");
                }
                //打包完成，不需要在本地再执行shell，退出这层远程登录，并登录远程和服务器
                localRunner.runCommand("rm -f BuildFrontend.sh");
                // localRunner.runCommand("rm -f LoginAuth.sh");
                localRunner.exit();

                String remoteDeployPath = propertyService.getValueByType(propertyService.getServerInfo(deployLog.getTarget()),Constant.PropertyType.DEPLOY_PATH);
                //确认可以登录远程服务器
                remoteRunner = new ShellRunner(deployLog.getTarget(), propertyService.getValueByType(serverInfoList, Constant.PropertyType.USERNAME),
                        propertyService.getValueByType(serverInfoList,Constant.PropertyType.PASSWORD));
                remoteRunner.login();
                remoteRunner.runCommand("mkdir -p "+remoteDeployPath);
                remoteRunner.runCommand("rm -f "+remoteDeployPath+"/*.tar.gz");
                //打包完成，上传包到远程服务器
                uploadService.uploadFiles(remoteRunner, frontendPath, remoteDeployPath,"tar.gz");
                //上传完成，在远程服务器进行部署
                uploadService.uploadScript(remoteRunner, "DeployFrontend.sh", "frontend");
                //2.整理部署结果
                String args = ShellRunner.appendArgs(new String[]{
                        propertyService.getValueByType(serverInfoList,Constant.PropertyType.DEPLOY_PATH),
                        propertyService.getValueByType(serverInfoList,Constant.PropertyType.BACKUP_PATH)});
                try {
                    String runningPath = propertyService.getValueByType(serverInfoList,Constant.PropertyType.RUN_PATH);
                    args += (" "+runningPath);
                } catch (MyException defaultTomcat) {
                    logger.info("使用Tomcat进程指定的路径打包");
                }
                if (remoteRunner.runCommand("sh DeployFrontend.sh"+args)) {
                    deployResult.append("部署成功 ");
                } else {
                    deployResult.append("部署异常 ");
                }
                if (remoteRunner.getError().length() > 0 )
                    deployResult.append("错误信息:"+remoteRunner.getError());
            } catch (MyException e) {
                logger.error(e.getMessage());
                deployResult.append("部署错误！\n"+e.getMessage());
            } finally {
                if (remoteRunner != null) {
                    try {
                        remoteRunner.runCommand("rm -f DeployFrontend.sh");
                        remoteRunner.exit();
                    } catch (MyException e) {
                        e.printStackTrace();
                    }
                }
            }
            //3.全工程clear，写日志到数据库
            deployLog.setEndTime(new Date());
            deployLog.setResult(deployResult.toString());
            historyMapper.insertSelective(deployLog);
            logger.info("--- 部署完成，已存储记录 ---");
            // emailService.sendSimpleEmail(phone,serverIP+"部署结束", deployResult.toString());
        }).start();
    }

    /**
     * 打包单个前端.
     * @param shellRunner 本地连接
     * @param gitRepository 要打包的git仓库
     * @param gitBranch 要checkout到的git分支
     * @param npmScript 打包运行的脚本
     * @param filename 打包后的dist要打包成的文件名：filename.tar.gz --(解压)--> filename/index.html
     * @return 打包信息，是否打包成功
     * */
    public String buildFrontend(@NotNull ShellRunner shellRunner, String gitRepository, String gitBranch, String npmScript, String localPath, String filename) throws MyException {
        logger.debug("前端："+gitRepository+" 打包中");
        if (!isPacking(shellRunner, gitRepository)) {
            shellRunner.runCommand("sh BuildFrontend.sh"
                    + ShellRunner.appendArgs(new String[]{localGitPath,gitRepository,gitBranch,npmScript,localPath,filename}),null);
            logger.debug("前端："+gitRepository+" 打包完成");
            if (shellRunner.isSuccess())
                return "打包成功\n"+shellRunner.getError();
            else
                return "打包异常\n"+shellRunner.getError();
        } else {
            return "打包失败:"+gitRepository+"正在打包，请稍后重试";
        }
    }

    /**
     * 回滚前端.
     * 因为回滚比较快，所以直接返回
     * 复制文件可能会用一些时间，一个前端包的赋值应该不会用太长时间
     * */
    public void rollbackFrontend(ShellRunner remoteRunner, List<Property> serverInfoList, @NotNull List<RollbackFrontendEntity> rollbackList) throws MyException {
        History deployLog = deployUtil.createLogEntity(remoteRunner);
        logger.info("开始回滚："+deployLog.getTarget());
        StringBuffer deployResult = new StringBuffer();
        deployResult.append("部署类型：从备份回滚前端\n");
        deployResult.append("目标服务器："+deployLog.getTarget()+"\n");
        deployResult.append("所有回滚：\n");
        for (int i = 0; i < rollbackList.size(); i++) {
            RollbackFrontendEntity dataDTO = rollbackList.get(i);
            // @param 2.备份父文件夹
            // @param 3.父文件夹下的以时间作名字的具体备份文件夹
            // @param 4.要部署到webapps文件夹底下的文件夹名，一般来讲与备份父文件夹同名，不排除需要自定义的场景
            String tarDir = (dataDTO.getTarDir() == null || "".equalsIgnoreCase(dataDTO.getTarDir()))?dataDTO.getName():dataDTO.getTarDir();
            String backupPath = propertyService.getValueByType(serverInfoList, Constant.PropertyType.BACKUP_PATH);
            if (remoteRunner.runCommand("sh RollbackFrontend.sh" +
                    ShellRunner.appendArgs(new String[]{backupPath, dataDTO.getName(), dataDTO.getTarBackup(), tarDir}) )) {
                deployResult.append("回滚["+dataDTO.getName()+"/"+dataDTO.getTarBackup()+"]到【"+tarDir+"】成功\n");
            } else {
                deployResult.append("回滚["+dataDTO.getName()+"/"+dataDTO.getTarBackup()+"]到【"+tarDir+"】异常\n");
            }
            deployResult.append("错误信息："+remoteRunner.getError());
        }
        //3.全工程clear，写日志到数据库
        deployLog.setEndTime(new Date());
        deployLog.setResult(deployResult.toString());
        historyMapper.insertSelective(deployLog);
        logger.info("--- 回滚完成，已存储记录 ---");
    }

    /**
     * 检查目录下是否有在进行打包.
     * 检查目录下是否有node或maven进程
     * @param shellRunner 远程登录连接
     * @param gitRepository git仓库文件夹
     * @return 该目录下是否有活跃的打包进程
     * */
    public boolean isPacking(@NotNull ShellRunner shellRunner, String gitRepository) throws MyException {
        /* 脚本执行中但还没有执行打包命令时不适用
        // node.js进程
        shellRunner.runCommand("ps -ef|grep [n]ode|grep '"+gitRepository+"' | wc -l");
        String retNode = (shellRunner.getResult()==null || shellRunner.getResult().size() == 0)?"0":(shellRunner.getResult().get(0));
        // npm进程
        shellRunner.runCommand("ps -ef|grep [n]pm|grep '"+gitRepository+"' | wc -l");
        String retNpm = (shellRunner.getResult()==null || shellRunner.getResult().size() == 0)?"0":(shellRunner.getResult().get(0));
        // maven进程
        shellRunner.runCommand("ps -ef|grep [m]vn|grep '"+gitRepository+"' | wc -l");
        String retMaven = (shellRunner.getResult()==null || shellRunner.getResult().size() == 0)?"0":(shellRunner.getResult().get(0));

        logger.info("仓库["+gitRepository+"]打包进程:[node:"+retNode+" npm:"+retNpm+" mvn:"+retMaven+"]");
        return !(retNode.equalsIgnoreCase("0") && retNpm.equalsIgnoreCase("0") && retMaven.equalsIgnoreCase("0"));
         */
        // shell脚本进程
        shellRunner.runCommand("ps -ef|grep -v grep|grep 'sh '|grep '"+gitRepository+"' | wc -l");
        String retAny = (shellRunner.getResult()==null || shellRunner.getResult().size() == 0)?"0":(shellRunner.getResult().get(0));

        logger.info("仓库["+gitRepository+"]打包脚本进程:"+retAny);
        return !retAny.equalsIgnoreCase("0");
    }

    private void swap(String[] arr, int x1, int x2) {
        String temp = arr[x1];
        arr[x1] = arr[x2];
        arr[x2] = temp;
    }
}
