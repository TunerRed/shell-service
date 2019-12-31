package org.shelltest.service.controller;

import org.jetbrains.annotations.NotNull;
import org.shelltest.service.dto.ServerDTO;
import org.shelltest.service.entity.*;
import org.shelltest.service.exception.MyException;
import org.shelltest.service.mapper.HistoryMapper;
import org.shelltest.service.mapper.PropertyMapper;
import org.shelltest.service.mapper.RepoMapper;
import org.shelltest.service.mapper.UserMapper;
import org.shelltest.service.services.PropertyService;
import org.shelltest.service.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("/system/config")
@RestController
public class ConfigController {

    @Autowired
    UserMapper userMapper;
    @Autowired
    RepoMapper repoMapper;
    @Autowired
    PropertyMapper propertyMapper;
    @Autowired
    PropertyService propertyService;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @PostMapping("/updateUser")
    public ResponseEntity updateUser(@Valid @RequestBody User user) throws MyException {
        User oriUser = userMapper.selectByPrimaryKey(user.getUsername());
        if (oriUser == null)
            throw new MyException(Constant.ResultCode.NOT_FOUND, "未找到用户");
        throw new MyException(Constant.ResultCode.DEVELOPING, "未开发，旧密码认证");
//        user.setPassword(EncUtil.encode(EncUtil.decodeUserPass(user.getPassword())));
//        userMapper.updateByPrimaryKeySelective(user);
//        return new ResponseBuilder().getResponseEntity();
    }

    @PostMapping("/addUser")
    public ResponseEntity addUser(@Valid @RequestBody User user) throws MyException {
        if (userMapper.selectByPrimaryKey(user.getUsername()) != null)
            throw new MyException(Constant.ResultCode.ARGS_ERROR, "已有账户");
        user.setPassword(EncUtil.encode(EncUtil.decodeUserPass(user.getPassword())));
        userMapper.insertSelective(user);
        return new ResponseBuilder().getResponseEntity();
    }

    @PostMapping("/updateServer")
    public ResponseEntity updateServer(@Valid @RequestBody ServerDTO server) throws MyException {
        ShellRunner loginTest = new ShellRunner(server.getIp(), server.getUsername(),
                EncUtil.encode(EncUtil.decodeUserPass(server.getPassword())));
        try {
            loginTest.login();
            loginTest.exit();
        } catch (MyException e) {
            throw new MyException(e.getResultCode(), "主机密码认证失败");
        }
        propertyService.updateServerInfo(server);
        return new ResponseBuilder().getResponseEntity();
    }

    @PostMapping("/addServer")
    public ResponseEntity addServer(@Valid @RequestBody ServerDTO server) throws MyException {
        PropertyExample example = new PropertyExample();
        example.createCriteria().andTypeEqualTo(Constant.PropertyType.IP).andValEqualTo(server.getIp());
        if (propertyMapper.selectByExample(example).size() != 0)
            throw new MyException(Constant.ResultCode.ARGS_ERROR, "已有主机");
        ShellRunner loginTest = new ShellRunner(server.getIp(), server.getUsername(),
                EncUtil.encode(EncUtil.decodeUserPass(server.getPassword())));
        try {
            loginTest.login();
            loginTest.exit();
        } catch (MyException e) {
            throw new MyException(e.getResultCode(), "主机密码认证失败");
        }
        propertyService.insertNewServer(server);
        return new ResponseBuilder().getResponseEntity();
    }

    @PostMapping("/updateRepo")
    public ResponseEntity updateRepo(@NotNull @RequestBody Repo repo) {
        RepoExample example = new RepoExample();
        example.createCriteria().andRepoEqualTo(repo.getRepo());
        repoMapper.updateByExampleSelective(repo, example);
        return new ResponseBuilder().getResponseEntity();
    }

    @PostMapping("/addRepo")
    public ResponseEntity addRepo(@NotNull @RequestBody Repo repo) throws MyException {
        RepoExample example = new RepoExample();
        example.createCriteria().andRepoEqualTo(repo.getRepo());
        if (repoMapper.selectByExample(example).size() > 0)
            throw new MyException(Constant.ResultCode.ARGS_ERROR, "已有仓库");
        repoMapper.insertSelective(repo);
        return new ResponseBuilder().getResponseEntity();
    }
}
