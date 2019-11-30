package org.shelltest.service.services;

import org.jetbrains.annotations.NotNull;
import org.shelltest.service.dto.RollbackFrontendEntity;
import org.shelltest.service.dto.BuildEntity;
import org.shelltest.service.entity.History;
import org.shelltest.service.entity.Property;
import org.shelltest.service.exception.MyException;
import org.shelltest.service.mapper.HistoryMapper;
import org.shelltest.service.utils.Constant;
import org.shelltest.service.utils.DeployLogUtil;
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
    HistoryMapper historyMapper;
    @Autowired
    DeployLogUtil deployUtil;

    @Value("${config.git.url}")
    String git_url;
    @Value("${config.git.username}")
    String git_user;
    @Value("${config.git.password}")
    String git_password;
    @Value("${local.gitpath}")
    String localGitPath;

    private Logger logger = LoggerFactory.getLogger(this.getClass());



    public void rollbackFrontend(ShellRunner remoteRunner, List<Property> serverInfoList, @NotNull List<RollbackFrontendEntity> rollbackList) throws MyException {
        History deployLog = deployUtil.createLogEntity(serverInfoList.get(0).getKey());
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
            String backupPath = propertyService.getPropertyValueByType(serverInfoList, Constant.PropertyType.BACKUP_PATH);
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
        deployResult.append("--- 回滚完成，已存储记录 ---");
    }

    public void buildFrontendThread(ShellRunner localRunner, String serverIP, BuildEntity[] deployList) throws MyException {
        List<Property> serverInfoList = propertyService.getServerInfo(serverIP);
        // 记录犯罪证据.jpg
        History deployLog = deployUtil.createLogEntity(serverIP);
        new Thread(()->{
            StringBuffer deployResult = new StringBuffer();
            ShellRunner remoteRunner = null;
            try {
                logger.info("--- 处理打包请求 ---");
                deployResult.append("部署类型：从Git部署前端\n");
                deployResult.append("目标服务器："+serverIP+"\n");
                deployResult.append("所有打包：\n");
                //在本机运行shell进行打包
                for (int i = 0; i < deployList.length; i++) {
                    deployResult.append("【"+(i+1)+"】"+deployList[i].getRepo()+":"
                            +deployList[i].getBranch()+":"+deployList[i].getFilename()+"\n");
                    String buildInfo = buildFrontend(localRunner, deployList[i].getRepo(), deployList[i].getBranch(),
                            deployList[i].getScript(), deployList[i].getFilename());
                    deployResult.append("打包结果：" + buildInfo + "\n");
                }
                //打包完成，不需要在本地再执行shell，退出这层远程登录，并登录远程和服务器
                localRunner.runCommand("rm -f BuildFrontend.sh");
                // localRunner.runCommand("rm -f LoginAuth.sh");
                localRunner.exit();

                //1.整理打包结果
                //确认可以登录远程服务器
                remoteRunner = new ShellRunner(serverIP, propertyService.getPropertyValueByType(serverInfoList, Constant.PropertyType.USERNAME),
                        propertyService.getPropertyValueByType(serverInfoList,Constant.PropertyType.PASSWORD));
                remoteRunner.login();
                //打包完成，上传包到远程服务器
                uploadService.uploadFiles(remoteRunner, localGitPath,
                        propertyService.getPropertyValueByType(propertyService.getServerInfo(serverIP),Constant.PropertyType.DEPLOY_PATH),"tar.gz");
                //上传完成，在远程服务器进行部署
                uploadService.uploadScript(remoteRunner, "DeployFrontend.sh", "frontend");
                //2.整理部署结果
                String args = ShellRunner.appendArgs(new String[]{
                        propertyService.getPropertyValueByType(serverInfoList,Constant.PropertyType.DEPLOY_PATH),
                        propertyService.getPropertyValueByType(serverInfoList,Constant.PropertyType.BACKUP_PATH)});
                if (remoteRunner.runCommand("sh DeployFrontend.sh"+args)) {
                    deployResult.append("部署成功 ");
                } else {
                    deployResult.append("部署异常 ");
                }
                if (remoteRunner.getError() != null)
                deployResult.append("错误信息:\n"+remoteRunner.getError()+"\n");
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
            deployResult.append("--- 部署完成，已存储记录 ---");
            // emailService.sendSimpleEmail(phone,serverIP+"部署结束", deployResult.toString());
        }).start();
    }

    public String buildFrontend(@NotNull ShellRunner shellRunner, String gitRepository, String gitBranch, String npmScript, String filename) throws MyException {
        logger.debug("前端："+gitRepository+" 打包中");
        if (!isPacking(shellRunner, gitRepository)) {
            shellRunner.runCommand("sh BuildFrontend.sh"
                    + ShellRunner.appendArgs(new String[]{git_url,git_user,git_password,localGitPath,gitRepository,gitBranch,npmScript,filename}),null);
            logger.debug("前端："+gitRepository+" 打包完成");
            if (shellRunner.isSuccess())
                return "打包成功\n"+shellRunner.getError();
            else
                return "打包异常\n"+shellRunner.getError();
        } else {
            return "打包异常:"+gitRepository+"正在打包，请稍后重试";
        }
    }

    /**
     * 检查目录下是否有在进行打包.
     * 检查目录下是否有node或maven进程
     * @param shellRunner 远程登录连接
     * @param gitRepository git仓库文件夹
     * @return 该目录下是否有活跃的打包进程
     * */
    public boolean isPacking(@NotNull ShellRunner shellRunner, String gitRepository) throws MyException {
        shellRunner.runCommand("ps -ef|grep [n]ode|grep '"+gitRepository+"' | wc -l");
        String retNode = (shellRunner.getResult()==null || shellRunner.getResult().size() == 0)?"0":(shellRunner.getResult().get(0));

        shellRunner.runCommand("ps -ef|grep [m]vn|grep '"+gitRepository+"' | wc -l");
        String retMaven = (shellRunner.getResult()==null || shellRunner.getResult().size() == 0)?"0":(shellRunner.getResult().get(0));
        logger.info("maven进程运行数量 in "+gitRepository+" ： "+retMaven);

        return !(retNode.equalsIgnoreCase("0") && retMaven.equalsIgnoreCase("0"));
    }
}
