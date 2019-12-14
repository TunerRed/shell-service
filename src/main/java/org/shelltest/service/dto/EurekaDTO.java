package org.shelltest.service.dto;

import org.shelltest.service.exception.MyException;
import org.shelltest.service.utils.ShellRunner;

public class EurekaDTO {

    // 应用名，从数据库获取
    String name;

    // jar包名，从进程获取
    String jar;

    // 进程pid，从进程获取
    int pid = 0;

    // 应用端口，从eureka页面获取?
    int port = 0;

    // actuator信息，从接口获取
    String actuator = "developing：{'git':{'branch':'master','commit':{'id':'bbd1dd0','time':1575885315.000000000}}}";

    // 启动日期，从进程获取
    String startTime = "00-00";

    // 内存占用
    String mem;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJar() {
        return jar;
    }

    public void setJar(String jar) {
        this.jar = jar;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getActuator() {
        return actuator;
    }

    public void setActuator(String actuator) {
        this.actuator = actuator;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getMem() {
        return mem;
    }

    public void setMem(String mem) {
        this.mem = mem;
    }

    public void initDataFromProcess(ShellRunner remoteRunner, String filename) throws MyException {
        // pid/内存占用/启动月份/启动日期/jar包路径及包名
        if (remoteRunner == null || filename == null || filename.isEmpty())
            return;
        if (remoteRunner.runCommand("ps -eo pid,%mem,start,cmd|egrep [j]ava|egrep /"+filename+"-[0-9]{4}.jar$" +
                "|awk '{print $1,$2,$3,$4,$NF}'") && remoteRunner.getResult() != null) {
            String[] info = remoteRunner.getResult().get(0).split(" ");
            if (info!=null && info.length==5) {
                // setPid(Integer.parseInt(info[0]));
                setMem(info[1]);
                setStartTime(info[2]+"-"+info[3]);
            }
        }
    }
}
