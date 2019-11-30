package org.shelltest.service.utils;

import org.apache.commons.lang.StringUtils;
import org.shelltest.service.entity.History;
import org.shelltest.service.services.LoginAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 记录犯罪证据.avi
 * 放一些写日志时会用到的方法
 * */
@Service
public class DeployLogUtil {

    @Autowired
    HttpServletRequest request;
    @Autowired
    LoginAuth loginAuth;

    /**
     * 获取真实ip
     * 如果通过NGINX做了转发，NGINX中要配置'X-Real_IP'等字段
     * */
    public static String getRealIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Real_IP");
        if (StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip))
            return ip;
        ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            int index = ip.indexOf(",");
            if (index != -1)
                return ip.substring(0,index);
            return ip;
        }
        String[] checkChain = new String[]{"Proxy-Client-Ip","WL-Proxy-Client-Ip","HTTP_CLIENT_IP","HTTP_X_FORWARDED_FOR"};
        for (int i = 0; i < checkChain.length; i++) {
            if (isNotValid(ip))
                ip = request.getHeader(checkChain[i]);
        }
        if (isNotValid(ip))
            ip = request.getRemoteAddr();
        return isNotValid(ip)?"":ip;
    }
    private static boolean isNotValid(String ip) {
        return ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip);
    }


    /**
     * 快速生成一个log记录
     * @param serverIP 要部署的目标主机，关键信息
     * */
    public History createLogEntity(String serverIP) {
        // 记录犯罪证据.jpg
        History deployLog = new History();
        deployLog.setIp(getRealIP(request));
        deployLog.setTarget(serverIP);
        deployLog.setUser(loginAuth.getUser(request.getHeader(Constant.RequestArg.Auth)));
        // 从此时记录开始时间
        deployLog.setStartTime(new Date());

        return deployLog;
    }
}
