package org.shelltest.service.services;

import io.jsonwebtoken.*;
import org.shelltest.service.exception.LoginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class LoginAuth {

    @Value("${token.config.secret}")
    String secret;
    @Value("${token.config.iss}")
    String issUser;
    @Value("${token.config.expiresSeconds}")
    long expiresSeconds;
    @Value("${token.config.expiresMinutes}")
    int expiresMinutes;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public String createToken (String username) {
        long curSeconds = System.currentTimeMillis();
        long expiresMillSecond = (expiresMinutes * 60 + expiresSeconds) * 1000;
        JwtBuilder jwtBuilder = Jwts.builder().claim("loginTime",new Date(curSeconds)).setSubject(username)
                .setIssuer(issUser).signWith(SignatureAlgorithm.HS512,secret)
                .setExpiration(new Date(curSeconds + expiresMillSecond)).setNotBefore(new Date(curSeconds));
        logger.debug("用户[" + username + "]登录，token有效期：" + (expiresMillSecond/1000) + "秒");
        return jwtBuilder.compact();
    }

    public String verify (String token) throws LoginException {
        String username = null;
        try {
            Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
            username = claims.getSubject();
            Date loginTime = claims.get("loginTime",Date.class);
            if(username.isEmpty() || loginTime == null)
                throw new Exception();
        } catch (ExpiredJwtException e){
            throw new LoginException("登录过期");
        } catch (Exception e) {
            // secret==null || parse Error || username == null
            logger.debug("token认证失败："+e.getMessage());
            throw new LoginException("认证失败，请重新登录");
        }
        return username;
    }

    public String getUser(String token) {
        String username = "";
        try {
            username = verify(token);
        } catch (Exception e) {
            logger.error("token认证失败："+e.getMessage());
        }
        return username;
    }
}
