package org.shelltest.service.dto;

import java.util.List;

public class RollbackFrontendDTO {

    String serverIP;

    List<RollbackFrontendEntity> rollbackData;

    public String getServerIP() {
        return serverIP;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    public List<RollbackFrontendEntity> getRollbackData() {
        return rollbackData;
    }

    public void setRollbackData(List<RollbackFrontendEntity> rollbackData) {
        this.rollbackData = rollbackData;
    }
}
