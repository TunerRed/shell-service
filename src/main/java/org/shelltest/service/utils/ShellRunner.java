package org.shelltest.service.utils;

import ch.ethz.ssh2.ChannelCondition;
import org.shelltest.service.exception.MyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

/**
 * 运行脚本的工具类，包括登录和退出.
 * 同一个账户可以多次登录，但是同一时刻只能运行一个持续性的脚本（Thread）
 * 并且，一次登录在执行了持续性脚本后，只有等到脚本结束才能执行其他指令
 * @author codev
 * */
public class ShellRunner {
    private String host;
    private String username;
    private String password;

    private boolean success;

    private Connection conn = null;
    private LinkedList<String> resultMsg;
    private StringBuffer errorMsg;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public ShellRunner(String host, String username, String password) {
        this.host=host;
        this.username=username;
        this.password=BASE64Util.decode(BASE64Util.decode(password));
        resultMsg=new LinkedList<>();
        errorMsg=new StringBuffer();
    }

    /**
     * 登录远程桌面.
     * 执行命令前先确定有成功登录
     * @return 是否登录成功
     * */
    public boolean login() throws MyException {
        try {
            conn = new Connection(host);
            conn.connect();
            if (conn.authenticateWithPassword(username,password)){
                logger.info("==========================");
                logger.info("登录成功:"+conn.getHostname());
            } else {
                logger.error("登录失败["+host+"]:"+conn.getConnectionInfo());
                throw new MyException(Constant.ResultCode.LOGIN_FAILED, "登录["+host+"]认证失败|");
            }
        } catch (IOException e) {
            logger.error("登录失败:"+e.getMessage());
            exit();
            throw new MyException(Constant.ResultCode.LOGIN_FAILED, "远程登录失败："+host);
        }
        return true;
    }

    /**
     * 退出远程桌面.
     * */
    public void exit() throws MyException {
        if (conn != null) {
            try {
                conn.close();
                conn = null;
                logger.info("退出登录");
            } catch (Exception e){
                logger.error("退出登录失败:"+e.getMessage());
                throw new MyException(Constant.ResultCode.LOGIN_FAILED, "远程退出失败？");
            }
        }
    }

    /**
     * 在远程桌面，执行命令并获取输出.
     * @param cmd 要执行的命令
     * @return 命令是否执行成功
     * */
    private boolean runCommandImmadiately(String cmd, Callback callback) throws MyException {
        resultMsg.clear();
        if (errorMsg != null && errorMsg.length() > 0)
            errorMsg.delete(0, errorMsg.length()-1);
        Session session = null;
        success = false;
        try {
            session = conn.openSession();
            session.execCommand(cmd);
            BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(new StreamGobbler(session.getStdout()),"UTF-8"));
            BufferedReader stderrReader = new BufferedReader(new InputStreamReader(new StreamGobbler(session.getStderr()),"UTF-8"));
            String line=null;
            while ((line=stdoutReader.readLine())!=null) {
                logger.info(line);
                resultMsg.add(line);
            }
            while ((line=stderrReader.readLine())!=null) {
                logger.error(line);
                errorMsg.append(line+"\n");
            }
            // logger.debug("脚本执行完成");
            success = (session.getExitStatus() == 0);
        } catch (IOException e) {
            logger.error("Session执行失败" + e.getMessage());
            throw new MyException(Constant.ResultCode.INTERNAL_ERROR, "远程会话异常："+e.getMessage());
        } catch (NullPointerException e) {
            logger.error("空指针："+e.getMessage());
        } catch (Exception e) {
            logger.error("wdnmd ShellRunner又双叒叕意外的Exception");
            errorMsg.append("ShellRunner又双叒叕意外的Exception"+e.getMessage()+"\n");
            e.printStackTrace();
        }finally {
            if (session != null) {
                session.close();
            }
            if (callback != null)
                callback.callback(this);
        }
        return success;
    }

    public boolean runCommand(String cmd) throws MyException {
        return runCommand(cmd,null);
    }
    public boolean runCommand(String cmd, Callback callback) throws MyException {
        return runCommandImmadiately(cmd,callback);
    }

    /**
     * 获取shell执行的输出信息.
     * @return  shell执行的输出信息
     * */
    public LinkedList<String> getResult() {
        return (LinkedList<String>) resultMsg.clone();
    }
    public String[] getResultList() {
        return resultMsg.toString().split("\n");
    }

    /**
     * 获取shell执行的错误信息.
     * 在脚本中会将错误以 1>&2 的形式输出
     * 多数情况下脚本会直接'exit 1'
     * @return  shell执行的错误信息
     * */
    public String getError() {
        return errorMsg.toString().length() > 10000?errorMsg.toString().substring(0, 10000):errorMsg.toString();
    }

    /**
     * 拼接参数的小工具.
     * @param args 命令参数
     * */
    public static String appendArgs(String[] args) throws MyException {
        StringBuffer argsBuffer = new StringBuffer();
        boolean _throw = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null || args[i].isEmpty())
                _throw = true;
            argsBuffer.append(" "+args[i]);
        }
        if (_throw) {
            throw new MyException(Constant.ResultCode.ARGS_ERROR, "脚本缺少必要的参数:"+argsBuffer.toString());
        }
        return argsBuffer.toString();
    }

    public boolean isSuccess() {
        return success;
    }
    public Connection getConn() {
        return conn;
    }
}
