package com.xzkj.health.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnProperty(name = "health.auth.enabled", havingValue = "true", matchIfMissing = true)
public class SaTokenConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(ignored -> {
            String path = SaHolder.getRequest().getRequestPath();
            String method = SaHolder.getRequest().getMethod();
            if (AuthPathRules.requiresAdmin(path, method)) {
                StpUtil.checkRole(AuthPathRules.ADMIN_ROLE);
            } else if (AuthPathRules.requiresLogin(path)) {
                StpUtil.checkLogin();
            }
        })).addPathPatterns("/**").excludePathPatterns("/error");
    }
}
