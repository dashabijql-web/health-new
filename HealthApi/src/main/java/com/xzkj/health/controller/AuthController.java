package com.xzkj.health.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xzkj.health.model.entity.SysUser;
import com.xzkj.health.service.SysUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final SysUserService sysUserService;

    public AuthController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        SysUser user = sysUserService.login(request.getUsername(), request.getPassword());
        StpUtil.login(user.getId());
        return toUserMap(user, StpUtil.getTokenValue());
    }

    @PostMapping("/logout")
    public Map<String, String> logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
        return Map.of("message", "已退出");
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        SysUser user = sysUserService.getById(StpUtil.getLoginIdAsLong());
        return toUserMap(user, StpUtil.getTokenValue());
    }

    private Map<String, Object> toUserMap(SysUser user, String token) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token);
        data.put("username", user.getUsername());
        String name = user.getRealName() == null || user.getRealName().isBlank()
                ? user.getUsername()
                : user.getRealName();
        data.put("name", name);
        return data;
    }
}
