package org.shelltest.service.entity;

public class AvailBackup {

    public String getName() {
        return name;
    }

    public AvailBackup setName(String name) {
        this.name = name;
        return this;
    }

    public String getInfo() {
        return info;
    }

    public AvailBackup setInfo(String info) {
        this.info = info;
        return this;
    }

    private String name;
    private String info;
}
