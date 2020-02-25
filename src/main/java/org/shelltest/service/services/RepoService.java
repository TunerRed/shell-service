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
        if (repoName != null) {
            repositoryExample.createCriteria().andRepoEqualTo(repoName);
            List<Repo> list = repoMapper.selectByExample(repositoryExample);
            if (list != null && list.size() > 0) {
                return list.get(0);
            } else {
                throw new MyException(Constant.ResultCode.INTERNAL_ERROR, "未发现仓库！");
            }
        }
        return null;
    }

    /**
     * 获取查询到的所有仓库各自的远程git分支.
     * 用户进入页面时调用，为了不影响速度，默认不进行pull操作
     * @param shellRunner 登录本机
     * @param repositoryList 仓库列表
     * @return 填充了分支列表的仓库列表
     * */
    public List<Repo> getDecoratedRepos (ShellRunner shellRunner, List<Repo> repositoryList) throws MyException {
        uploadService.uploadScript(shellRunner,"ListAvailBranch.sh",null);
        for (int i = 0; i < repositoryList.size(); i++) {
            Repo repo = repositoryList.get(i);
            repo.setPacking(buildAppService.isPacking(shellRunner, repo.getRepo()));
            if (!repo.isPacking()) {
                logger.info("查找项目可用git分支："+repo.getRepo());
                List<String> availBranch = getAvailBranch(shellRunner,repo, false);
                repo.setBranchList(availBranch);
            }
        }
        shellRunner.runCommand("rm -f ListAvailBranch.sh");
        return repositoryList;
    }

    /**
     * 获取单个仓库的远程git分支.
     * 如果有新的分支时，需要在pull之后才能看到，否则显示的是最近一次pull时git已有的分支
     * @param shellRunner 登录本机
     * @param repo 仓库
     * @param pull 获取分支列表前是否先拉取代码，这可能会影响到正在打包的进程
     * @return 填充了分支列表的仓库
     * */
    public List<String> getAvailBranch (ShellRunner shellRunner, Repo repo, boolean pull) throws MyException {
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

    /**
     * 获取前端vue仓库可运行的node脚本.
     * @param shellRunner 登录本机
     * @param repo 仓库
     * @param branch 指定分支，切换后查询脚本
     * */
    public List<String> getAvailNpmScript (ShellRunner shellRunner, Repo repo, String branch) throws MyException {
        String args = ShellRunner.appendArgs(new String[]{GIT_path+"/"+repo.getRepo(),branch});
        if (!shellRunner.runCommand("sh GitCheckout.sh" + args)) {
            throw new MyException(Constant.ResultCode.SHELL_ERROR, "切换分支 执行错误:\n"+shellRunner.getError());
        }

        List<String> result = null;
        args = ShellRunner.appendArgs(new String[]{GIT_path+"/"+repo.getRepo()});
        result = null;
        // 分析package.json文件内的内容以获取全部以"build"开头的脚本
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
