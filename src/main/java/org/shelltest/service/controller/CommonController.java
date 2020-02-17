package org.shelltest.service.controller;

import org.apache.ibatis.annotations.Param;
import org.shelltest.service.dto.StatisticEntity;
import org.shelltest.service.entity.History;
import org.shelltest.service.entity.HistoryExample;
import org.shelltest.service.entity.Property;
import org.shelltest.service.entity.User;
import org.shelltest.service.exception.LoginException;
import org.shelltest.service.mapper.HistoryMapper;
import org.shelltest.service.mapper.UserMapper;
import org.shelltest.service.services.LoginAuth;
import org.shelltest.service.services.PropertyService;
import org.shelltest.service.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

@RequestMapping("/common")
@RestController
public class CommonController {

    @Autowired
    PropertyService propertyService;
    @Autowired
    LoginAuth loginAuth;
    @Autowired
    OtherUtil otherUtil;
    @Autowired
    HistoryMapper historyMapper;
    @Autowired
    UserMapper userMapper;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/message-list")
    public ResponseEntity getDeployMessage(@RequestParam("count") int limit) {
//        List<History> totalList = historyMapper.selectMessage(limit);
        if (limit <= 0) {
            limit = 10;
        }
        int notReadCount;
        List<History> totalList = historyMapper.selectNotRead(limit);
        List<History> readList;
        if (totalList == null) {
            totalList = new LinkedList<>();
        }
        notReadCount = totalList.size();
        if (notReadCount < limit) {
            readList = historyMapper.selectAlreadyRead(limit - notReadCount);
            totalList.addAll(readList);
        }
        return new ResponseBuilder().setData(totalList).getResponseEntity();
    }

    @GetMapping("/statistic")
    public ResponseEntity getStatistics() {
        List<StatisticEntity> list = historyMapper.getStatisticList(otherUtil.getFormatDateInMonth(-1,1),
                otherUtil.getFormatDateInMonth(1, 0));
        return new ResponseBuilder().putItem("dateList", list).getResponseEntity();
    }

    @GetMapping("/login")
    public ResponseEntity login (String username, String password) throws LoginException {
        String token;
        if (username == null || password == null) {
            throw new LoginException("登录失败，请重新登录");
        }
        else {
            User user = userMapper.selectByPrimaryKey(username);
            if (user == null)
                throw new LoginException(Constant.ResultCode.NOT_FOUND, "未注册的用户");
            String enc = EncUtil.encode(EncUtil.decodeUserPass(password.trim()));
            if (!enc.equals(user.getPassword()))
                throw new LoginException(Constant.ResultCode.NOT_FOUND, "密码错误");
            token = loginAuth.createToken(username);
        }
        return new ResponseBuilder().putItem("token", token).putItem("expiration", loginAuth.getExpiration().getTime()).getResponseEntity();
    }
}
