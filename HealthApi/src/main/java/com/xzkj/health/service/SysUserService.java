package com.xzkj.health.service;

import com.xzkj.health.mapper.SysUserMapper;
import com.xzkj.health.model.entity.SysUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SysUserService {
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    public SysUserService(SysUserMapper sysUserMapper, PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public SysUser login(String username, String password) {
        String normalizedName = username == null ? "" : username.trim();
        String rawPassword = password == null ? "" : password;
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (rawPassword.isBlank()) {
            throw new IllegalArgumentException("密码不能为空");
        }

        SysUser user = sysUserMapper.findByUsername(normalizedName);
        if (user == null || user.getPassword() == null || !passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() != 0) {
            throw new IllegalArgumentException("账号已禁用");
        }
        return user;
    }

    public SysUser getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }
}
