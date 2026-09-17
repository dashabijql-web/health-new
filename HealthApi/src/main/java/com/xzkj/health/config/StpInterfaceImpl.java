package com.xzkj.health.config;

import cn.dev33.satoken.stp.StpInterface;
import com.xzkj.health.service.SysUserService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StpInterfaceImpl implements StpInterface {
    private final SysUserService sysUserService;

    public StpInterfaceImpl(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return List.of();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return sysUserService.getRoleCodes(Long.valueOf(loginId.toString()));
    }
}
