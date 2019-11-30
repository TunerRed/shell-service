package org.shelltest.service.dto;

import java.util.List;

public class BackupEntity {

    public String getName() {
        return name;
    }

    public BackupEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getInfo() {
        return info;
    }

    public BackupEntity setInfo(String info) {
        this.info = info;
        return this;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    private String name;
    private List<String> list;
    private String info;
}
