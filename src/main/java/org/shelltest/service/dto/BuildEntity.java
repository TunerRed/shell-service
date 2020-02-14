package org.shelltest.service.dto;

public class BuildEntity {
    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public boolean isDeploy() {
        return deploy;
    }

    public void setDeploy(boolean deploy) {
        this.deploy = deploy;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    String repo;
    String branch;
    String filename;
    String location;
    String script;
    // 是否进行部署（后端依赖可能只打包用作依赖而不部署）
    boolean deploy;
}
