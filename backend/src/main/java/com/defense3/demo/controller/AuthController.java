package com.defense3.demo.controller;

import com.defense3.demo.common.Result;
import com.defense3.demo.dto.ChangePasswordRequest;
import com.defense3.demo.dto.LoginRequest;
import com.defense3.demo.dto.LoginResponse;
import com.defense3.demo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return Result.success();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Result<LoginResponse> getCurrentUser() {
        LoginResponse response = authService.getCurrentUserInfo();
        return Result.success(response);
    }
}
