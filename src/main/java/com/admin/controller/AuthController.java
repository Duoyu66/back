package com.admin.controller;

import com.admin.common.R;
import com.admin.dto.LoginRequest;
import com.admin.dto.PasswordRequest;
import com.admin.dto.ProfileUpdateRequest;
import com.admin.dto.RegisterRequest;
import com.admin.dto.UserPreferencesRequest;
import com.admin.dto.VerifyPasswordRequest;
import com.admin.vo.UserPreferencesVO;
import com.admin.service.AuthService;
import com.admin.service.LoginLogService;
import com.admin.vo.LoginVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final LoginLogService loginLogService;

    @PostMapping("/register")
    public R<LoginVO> register(@Validated @RequestBody RegisterRequest request,
                               HttpServletRequest httpRequest) {
        LoginVO vo = authService.register(request, httpRequest);
        loginLogService.record(request.getUsername(), true, "注册并登录", httpRequest);
        return R.ok(vo);
    }

    @PostMapping("/login")
    public R<LoginVO> login(@Validated @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            LoginVO vo = authService.login(request, httpRequest);
            loginLogService.record(request.getUsername(), true, "登录成功", httpRequest);
            return R.ok(vo);
        } catch (BadCredentialsException e) {
            loginLogService.record(request.getUsername(), false, "用户名或密码错误", httpRequest);
            throw e;
        }
    }

    @PostMapping("/logout")
    public R<Void> logout(HttpServletRequest httpRequest) {
        try {
            String username = com.admin.util.SecurityUtils.currentUser().getUsername();
            authService.logoutCurrentSession();
            loginLogService.record(username, true, "退出登录", httpRequest);
        } catch (Exception ignored) {
            // 未登录时忽略
        }
        return R.ok();
    }

    @GetMapping("/info")
    public R<LoginVO> info() {
        return R.ok(authService.getCurrentUserInfo());
    }

    @PutMapping("/profile")
    public R<Void> updateProfile(@Validated @RequestBody ProfileUpdateRequest request) {
        authService.updateProfile(request);
        return R.ok();
    }

    @PutMapping("/password")
    public R<Void> updatePassword(@Validated @RequestBody PasswordRequest request) {
        authService.updatePassword(request);
        return R.ok();
    }

    @GetMapping("/preferences")
    public R<UserPreferencesVO> getPreferences() {
        return R.ok(authService.getPreferences());
    }

    @PutMapping("/preferences")
    public R<UserPreferencesVO> updatePreferences(@Validated @RequestBody UserPreferencesRequest request) {
        return R.ok(authService.updatePreferences(request));
    }

    @PostMapping("/verify-password")
    public R<Void> verifyPassword(@Validated @RequestBody VerifyPasswordRequest request) {
        authService.verifyPassword(request);
        return R.ok();
    }
}
