package org.shelltest.service.dto;

public class RollbackFrontendEntity {
    String name;
    String tarBackup;
    String tarDir;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTarBackup() {
        return tarBackup;
    }

    public void setTarBackup(String tarBackup) {
        this.tarBackup = tarBackup;
    }

    public String getTarDir() {
        return tarDir;
    }

    public void setTarDir(String tarDir) {
        this.tarDir = tarDir;
    }
}
