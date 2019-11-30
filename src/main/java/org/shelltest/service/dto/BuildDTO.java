package org.shelltest.service.dto;

import org.jetbrains.annotations.NotNull;
import org.shelltest.service.entity.BuildEntity;

public class BuildDTO {
    public String getServerIP() {
        return serverIP;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    public BuildEntity[] getDeployList() {
        return deployList;
    }

    public void setDeployList(BuildEntity[] deployList) {
        this.deployList = deployList;
    }

    @NotNull
    private String serverIP;
    private BuildEntity[] deployList;
}
