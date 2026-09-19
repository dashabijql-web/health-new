package com.xzkj.health.service;

import cn.dev33.satoken.stp.StpUtil;
import com.xzkj.health.model.entity.SysUser;
import org.springframework.stereotype.Service;

@Service
public class CurrentOperatorService {
    private final SysUserService sysUserService;

    public CurrentOperatorService(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    public String name() {
        SysUser user = sysUserService.getById(StpUtil.getLoginIdAsLong());
        if (user == null) return "未知用户";
        return user.getRealName() == null || user.getRealName().isBlank()
                ? user.getUsername() : user.getRealName();
    }
}
