package org.shelltest.service.entity;

import java.io.Serializable;
import java.util.List;

public class Repo implements Serializable {

    private boolean deploy;

    private String repoType;

    private String repo;

    private String filename;

    private String location;

    private List<String> branchList;

    private List<String> scriptList;

    private boolean packing;

    private static final long serialVersionUID = 1L;

    public String getRepoType() {
        return repoType;
    }

    public void setRepoType(String repoType) {
        this.repoType = repoType == null ? null : repoType.trim();
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo == null ? null : repo.trim();
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename == null ? null : filename.trim();
    }

    public List<String> getBranchList() {
        return branchList;
    }

    public void setBranchList(List<String> branchList) {
        this.branchList = branchList;
    }

    public boolean isDeploy() {
        return deploy;
    }

    public void setDeploy(boolean deploy) {
        this.deploy = deploy;
    }

    public List<String> getScriptList() {
        return scriptList;
    }

    public void setScriptList(List<String> scriptList) {
        this.scriptList = scriptList;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isPacking() {
        return packing;
    }

    public void setPacking(boolean packing) {
        this.packing = packing;
    }
}
