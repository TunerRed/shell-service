package org.shelltest.service.controller;

import org.shelltest.service.dto.StatisticEntity;
import org.shelltest.service.entity.History;
import org.shelltest.service.entity.Property;
import org.shelltest.service.exception.LoginException;
import org.shelltest.service.mapper.HistoryMapper;
import org.shelltest.service.services.LoginAuth;
import org.shelltest.service.services.PropertyService;
import org.shelltest.service.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    HistoryMapper historyMapper;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/message-list")
    public ResponseEntity getDeployMessage() {
        logger.info("/common/message-list");
        int notReadCount = 0;
        List<History> totalList = historyMapper.selectNotRead(20);
        List<History> readList = null;
        if (totalList == null) {
            totalList = new LinkedList<>();
        }
        notReadCount = totalList.size();
        if (notReadCount < 10) {
            readList = historyMapper.selectAlreadyRead(10 - notReadCount);
            totalList.addAll(readList);
        }
        return new ResponseBuilder().setData(totalList).getResponseEntity();
    }

    @GetMapping("/statistic")
    public ResponseEntity getStatistics() {
        logger.info("/common/statistic");
        List<StatisticEntity> list = historyMapper.getStatisticList(OtherUtil.getFormatDateInMonth(-1,1),
                OtherUtil.getFormatDateInMonth(1, 0));
        return new ResponseBuilder().putItem("dateList", list).getResponseEntity();
    }

    @GetMapping("/login")
    public ResponseEntity login (String username, String password) throws LoginException {
        logger.info("/common/login");
        String token;
        if (username == null || password == null) {
            throw new LoginException("登录失败，请重新登录");
        }
        else {
            Property loginInfo = propertyService.getPropertyByKeys("LOGIN", username);
            if (loginInfo == null)
                throw new LoginException(Constant.ResultCode.NOT_FOUND, "未注册的用户");
            String enc = EncUtil.encode(EncUtil.decodeUserPass(password.trim()));
            if (!enc.equals(loginInfo.getVal()))
                throw new LoginException(Constant.ResultCode.NOT_FOUND, "密码错误");
            //logger.debug("登录成功，用户："+username+" "+Base64Utils.decode(loginInfo.getVal().getBytes()));
            token = loginAuth.createToken(username);
        }
        return new ResponseBuilder().putItem("token", token).putItem("expiration", loginAuth.getExpiration().getTime()).getResponseEntity();
    }
}
