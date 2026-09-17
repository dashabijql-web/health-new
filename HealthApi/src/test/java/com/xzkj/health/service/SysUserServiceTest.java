package com.xzkj.health.service;

import com.xzkj.health.mapper.SysUserMapper;
import com.xzkj.health.model.entity.SysUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SysUserServiceTest {
    @Mock
    SysUserMapper sysUserMapper;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    SysUserService sysUserService;

    @Test
    void logsInWhenUsernameAndPasswordMatch() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("hashed");
        user.setStatus(0);
        when(sysUserMapper.findByUsername("admin")).thenReturn(user);
        when(passwordEncoder.matches("admin123", "hashed")).thenReturn(true);

        SysUser loggedIn = sysUserService.login(" admin ", "admin123");

        assertEquals(1L, loggedIn.getId());
        assertEquals("admin", loggedIn.getUsername());
    }

    @Test
    void rejectsUnknownUserOrWrongPassword() {
        when(sysUserMapper.findByUsername("admin")).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> sysUserService.login("admin", "admin123"));

        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    void rejectsDisabledAccount() {
        SysUser user = new SysUser();
        user.setUsername("admin");
        user.setPassword("hashed");
        user.setStatus(1);
        when(sysUserMapper.findByUsername("admin")).thenReturn(user);
        when(passwordEncoder.matches("admin123", "hashed")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> sysUserService.login("admin", "admin123"));

        assertEquals("账号已禁用", exception.getMessage());
    }

    @Test
    void rejectsBlankUsername() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> sysUserService.login("  ", "admin123"));

        assertEquals("用户名不能为空", exception.getMessage());
    }

    @Test
    void returnsRoleCodesForUser() {
        when(sysUserMapper.findRoleCodes(1L)).thenReturn(List.of("SUPER_ADMIN"));

        assertEquals(List.of("SUPER_ADMIN"), sysUserService.getRoleCodes(1L));
    }
}
