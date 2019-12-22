package org.shelltest.service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class MvcInterceptorConfig extends WebMvcConfigurationSupport{

    @Autowired
    private LoginInterceptor loginInterceptor;
    @Autowired
    private ConfigInterceptor configInterceptor;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        // 多个拦截器组成一个拦截器链
        // addPathPatterns 用于添加拦截规则，/**表示拦截所有请求
        // excludePathPatterns 用户排除拦截
        logger.debug("Configuration add: loginInterceptor");
        registry.addInterceptor(loginInterceptor).addPathPatterns("/**")
                .excludePathPatterns("/test/**","/common/**","/backdoor/**");
        logger.debug("Configuration add: configInterceptor");
        registry.addInterceptor(configInterceptor).addPathPatterns("/system/config/**");

        super.addInterceptors(registry);
    }
}
