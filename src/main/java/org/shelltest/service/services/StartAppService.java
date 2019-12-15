package org.shelltest.service.services;

import org.jetbrains.annotations.NotNull;
import org.shelltest.service.exception.MyException;
import org.shelltest.service.utils.ShellRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 启动jar包.
 * */
@Service
public class StartAppService {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public boolean startService(@NotNull ShellRunner remoteRunner, String filename,String path, String args, String logPath) throws MyException {
        // args加引号防止只读取到一个-D，为了防止参数需要加单引号的，这里使用了双引号
        String cmd = "sh StartService.sh " + ShellRunner.appendArgs(new String[]{path, filename, "\""+args+"\"", logPath});
        return remoteRunner.runCommand(cmd);
    }

    /**
     * 杀死指定的进程.
     * @param remoteRunner 远程连接
     * @param filename jar包名，不含后缀，不含时间戳
     * @return 是否有正在运行的进程
     * */
    public boolean killService(ShellRunner remoteRunner, String filename) throws MyException {
        /**
         * 需要先杀该应用进程的根本原因是端口占用
         * 故应当根据端口找到应用pid并杀进程
         * */
        /*//如何科学区分端口号（$2）和进程号（$NF）？
        String app = "eureka";
        String port = "1024";
        if (remoteRunner.runCommand("netstat -nlp | grep [j]ava | grep LISTEN | grep "+app+" | awk '{print $2,$NF}' " +
                "| grep -E \""+port+" [0-9]{*}/java\" | awk '{print $2}'")
                && remoteRunner.getResult() != null) {
            String pid = remoteRunner.getResult().get(0);
            pid = pid.substring(0, pid.indexOf('/'));
            remoteRunner.runCommand("kill -9 "+pid);
        }*/
        // 如果同一个服务器上部署了两套服务，则会误杀
        int pid = getProcessPid(remoteRunner, filename);
        return (pid != 0 && remoteRunner.runCommand("kill -9 "+pid));
    }

    public int getProcessPid (ShellRunner remoteRunner, String filename) throws MyException {
        if (remoteRunner.runCommand("ps -ef | grep [j]ava | grep -E \""+filename+"-[0-9]{4}.jar$\" | awk '{print $2}'")
                && remoteRunner.getResult() != null) {
            String pid = remoteRunner.getResult().get(0);
            return Integer.parseInt(pid);
        }
        return 0;
    }
}
