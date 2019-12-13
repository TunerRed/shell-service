package org.shelltest.service.dto;

public class EurekaDTO {

    // 应用名，从数据库获取
    String name = "test";

    // jar包名，从进程获取
    String jar = "乌拉-test-1031.jar";

    // 进程pid，从进程获取
    int pid = 0;

    // 应用端口，从eureka页面获取?
    String port = "12345";

    // actuator信息，从接口获取
    String actuator = "{'git':{'branch':'master','commit':{'id':'bbd1dd0','time':1575885315.000000000}}}";

    // 已运行时间，从进程获取
    String runTime = "1111";

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

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getActuator() {
        return actuator;
    }

    public void setActuator(String actuator) {
        this.actuator = actuator;
    }

    public String getRunTime() {
        return runTime;
    }

    public void setRunTime(String runTime) {
        this.runTime = runTime;
    }
}
