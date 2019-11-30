package org.shelltest.service.dto;

import java.util.List;

public class RollbackFrontendDTO {

    String serverIP;

    List<RollFrontDataDTO> rollbackData;

    public String getServerIP() {
        return serverIP;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    public List<RollFrontDataDTO> getRollbackData() {
        return rollbackData;
    }

    public void setRollbackData(List<RollFrontDataDTO> rollbackData) {
        this.rollbackData = rollbackData;
    }
}
