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

import java.util.List;

@Service
public class RepoService {

    @Autowired
    RepoMapper repoMapper;

    @Autowired
    BuildAppService buildAppService;

    @Value("${local.gitpath}")
    String GIT_path;
    @Value("${config.git.username}")
    String GIT_user;
    @Value("${config.git.password}")
    String GIT_pass;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<Repo> getRepositoryByType (String repoType) {
        RepoExample repositoryExample = new RepoExample();
        repositoryExample.createCriteria().andRepoTypeEqualTo(repoType);
        return repoMapper.selectByExample(repositoryExample);
    }

    public List<String> getAvailBranch (ShellRunner shellRunner, Repo repo) throws MyException {
        String args = ShellRunner.appendArgs(new String[]{GIT_path+"/"+repo.getRepo(),GIT_user,GIT_pass});
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

    public List<String> getAvailNpmScript (ShellRunner shellRunner, Repo repo) throws MyException {
        return getAvailNpmScript(shellRunner,repo,"master");
    }

    public List<String> getAvailNpmScript (ShellRunner shellRunner, Repo repo, String branch) throws MyException {
        String args = ShellRunner.appendArgs(new String[]{GIT_path+"/"+repo.getRepo(),branch,GIT_user,GIT_pass});
        if (!shellRunner.runCommand("sh GitCheckout.sh" + args)) {
            throw new MyException(Constant.ResultCode.SHELL_ERROR, "切换分支 执行错误:\n"+shellRunner.getError());
        }

        List<String> result = null;
        args = ShellRunner.appendArgs(new String[]{GIT_path+"/"+repo.getRepo(),GIT_user,GIT_pass});
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
