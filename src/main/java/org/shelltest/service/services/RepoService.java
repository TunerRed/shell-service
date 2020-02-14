package org.shelltest.service.services;

import org.shelltest.service.entity.Repo;
import org.shelltest.service.entity.RepoExample;
import org.shelltest.service.exception.MyException;
import org.shelltest.service.mapper.RepoMapper;
import org.shelltest.service.utils.Constant;
import org.shelltest.service.utils.ShellRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;

@Service
public class RepoService {

    @Autowired
    RepoMapper repoMapper;

    @Autowired
    BuildAppService buildAppService;
    @Autowired
    UploadService uploadService;

    @Value("${local.path.git}")
    String GIT_path;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<Repo> getRepositoryByType (String repoType) {
        RepoExample repositoryExample = new RepoExample();
        if (repoType != null) {
            repositoryExample.createCriteria().andRepoTypeEqualTo(repoType);
        }
        return repoMapper.selectByExample(repositoryExample);
    }

    public Repo getRepositoryByName(@NotNull String repoName) throws MyException {
        RepoExample repositoryExample = new RepoExample();
        Repo repo = null;
        if (repoName != null) {
            repositoryExample.createCriteria().andRepoEqualTo(repoName);
            List<Repo> list = repoMapper.selectByExample(repositoryExample);
            if (list != null && list.size() == 1) {
                repo = list.get(0);
            } else if (list != null && list.size() > 1) {
                throw new MyException(Constant.ResultCode.INTERNAL_ERROR, "数据错误，发现重复的仓库名！");
            }
        }
        return repo;
    }

    public List<Repo> getDecoratedRepos (ShellRunner shellRunner, List<Repo> repositoryList, boolean pull) throws MyException {
        uploadService.uploadScript(shellRunner,"ListAvailBranch.sh",null);
        for (int i = 0; i < repositoryList.size(); i++) {
            logger.info("查找项目可用git分支："+repositoryList.get(i).getRepo());
            if (!buildAppService.isPacking(shellRunner, repositoryList.get(i).getRepo())) {
                List<String> availBranch = getAvailBranch(shellRunner,repositoryList.get(i), pull);
                repositoryList.get(i).setBranchList(availBranch);
            } else {
                repositoryList.get(i).setBranchList(null);
            }
            repositoryList.get(i).setDeploy(false);
        }
        shellRunner.runCommand("rm -f ListAvailBranch.sh");
        return repositoryList;
    }
    public List<String> getAvailBranch (ShellRunner shellRunner, Repo repo, boolean pull) throws MyException {
        uploadService.uploadScript(shellRunner,"ListAvailBranch.sh",null);
        String args = ShellRunner.appendArgs(new String[]{GIT_path+"/"+repo.getRepo(), String.valueOf(pull?1:0)});
        List<String> result = null;
        if (shellRunner.runCommand("sh ListAvailBranch.sh" + args)) {
            result = shellRunner.getResult();
            if (result==null || result.size() == 0)
                throw new MyException(Constant.ResultCode.SHELL_ERROR, "找不到可用分支");
        } else {
            throw new MyException(Constant.ResultCode.SHELL_ERROR, "脚本 ListAvailBranch 执行错误:\n"+shellRunner.getError());
        }
        return result;
    }
    public List<String> getAvailBranch (ShellRunner shellRunner, Repo repo) throws MyException {
        return getAvailBranch(shellRunner, repo, true);
    }

    public List<String> getAvailNpmScript (ShellRunner shellRunner, Repo repo) throws MyException {
        return getAvailNpmScript(shellRunner,repo,"master");
    }

    public List<String> getAvailNpmScript (ShellRunner shellRunner, Repo repo, String branch) throws MyException {
        String args = ShellRunner.appendArgs(new String[]{GIT_path+"/"+repo.getRepo(),branch});
        if (!shellRunner.runCommand("sh GitCheckout.sh" + args)) {
            throw new MyException(Constant.ResultCode.SHELL_ERROR, "切换分支 执行错误:\n"+shellRunner.getError());
        }

        List<String> result = null;
        args = ShellRunner.appendArgs(new String[]{GIT_path+"/"+repo.getRepo()});
        result = null;
        if (shellRunner.runCommand("sh ListAvailScript.sh" + args)) {
            result = shellRunner.getResult();
            if (result==null || result.size() == 0)
                throw new MyException(Constant.ResultCode.SHELL_ERROR, "找不到可用打包配置");
        } else {
            throw new MyException(Constant.ResultCode.SHELL_ERROR, "脚本 ListAvailScript 执行错误:\n"+shellRunner.getError());
        }
        return result;
    }
}
