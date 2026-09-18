package com.defense3.demo.controller;

import com.defense3.demo.common.PageResult;
import com.defense3.demo.common.Result;
import com.defense3.demo.dto.UserRequest;
import com.defense3.demo.dto.UserResponse;
import com.defense3.demo.entity.UserRole;
import com.defense3.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 分页获取用户列表（超级管理员）
     */
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<PageResult<UserResponse>> listUsers(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserResponse> page = userService.findAll(departmentId, role, keyword, pageable);
        return Result.success(PageResult.of(page));
    }

    /**
     * 获取教师列表（超管和院系管理员）
     */
    @GetMapping("/dept/teachers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_ADMIN', 'TEACHER')")
    public Result<List<UserResponse>> listTeachers(@RequestParam(required = false) Long departmentId) {
        return Result.success(userService.findTeachersByDepartment(departmentId));
    }

    /**
     * 获取所有管理员列表（所有DEPT_ADMIN角色）
     */
    @GetMapping("/admin/pure-admins")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<List<UserResponse>> listPureAdmins() {
        return Result.success(userService.findAllAdmins());
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<UserResponse> getUserById(@PathVariable Long id) {
        return Result.success(userService.findById(id));
    }

    /**
     * 创建用户（超级管理员）
     */
    @PostMapping("/admin/users")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        return Result.success(userService.create(request));
    }

    /**
     * 创建教师（院系管理员）
     */
    @PostMapping("/dept/teachers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_ADMIN')")
    public Result<UserResponse> createTeacher(@Valid @RequestBody UserRequest request) {
        request.setRole(UserRole.TEACHER);
        return Result.success(userService.create(request));
    }

    /**
     * 更新用户（超级管理员）
     */
    @PutMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<UserResponse> updateUser(@PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        return Result.success(userService.update(id, request));
    }

    /**
     * 更新教师（院系管理员）
     */
    @PutMapping("/dept/teachers/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_ADMIN')")
    public Result<UserResponse> updateTeacher(@PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        request.setRole(UserRole.TEACHER);
        return Result.success(userService.update(id, request));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    /**
     * 删除/移除管理员（可能降级）
     */
    @DeleteMapping("/admin/admins/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> deleteAdmin(@PathVariable Long id) {
        userService.removeAdmin(id);
        return Result.success();
    }

    /**
     * 启用/禁用用户
     */
    @PostMapping("/admin/users/{id}/toggle")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> toggleEnabled(@PathVariable Long id) {
        userService.toggleEnabled(id);
        return Result.success();
    }

    /**
     * 重置密码
     */
    @PostMapping("/admin/users/{id}/reset-password")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> resetPassword(@PathVariable Long id,
            @RequestParam(defaultValue = "123456") String password) {
        userService.resetPassword(id, password);
        return Result.success();
    }

    /**
     * 上传教师签名
     */
    @PostMapping("/users/{id}/signature")
    public Result<String> uploadSignature(@PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {
        return Result.success(userService.uploadSignature(id, file));
    }
}
