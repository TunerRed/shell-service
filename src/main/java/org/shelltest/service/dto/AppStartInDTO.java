package org.shelltest.service.dto;
import javax.validation.constraints.NotNull;
import java.util.List;

public class AppStartInDTO {
    @NotNull
    private String serverIP;
    @NotNull
    private List<String> filenames;

    public String getServerIP() {
        return serverIP;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    public List<String> getFilenames() {
        return filenames;
    }

    public void setFilenames(List<String> filenames) {
        this.filenames = filenames;
    }
}
