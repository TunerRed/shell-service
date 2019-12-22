package org.shelltest.service.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.shelltest.service.entity.User;
import org.shelltest.service.exception.LoginException;
import org.shelltest.service.mapper.UserMapper;
import org.shelltest.service.services.PropertyService;
import org.shelltest.service.utils.Constant;
import org.shelltest.service.services.LoginAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class ConfigInterceptor implements HandlerInterceptor {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    LoginAuth loginAuth;
    @Autowired
    UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String token = request.getHeader("Authorization");
        if (token != null) {
            String username = loginAuth.getUser(token);
            User root = userMapper.selectByPrimaryKey(username);
            if (root == null)
                throw new LoginException(Constant.ResultCode.NOT_FOUND, "无效用户");
            if (!root.getRoot().equals(Constant.SQLEnum.ROOT_USER))
                throw new LoginException(Constant.ResultCode.NOT_GRANT, "权限不足");
            else
                return true;
        }
        throw new LoginException(Constant.ResultCode.USER_LOGIN_FAILED, "请先登录");
    }
}
