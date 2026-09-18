package com.defense3.demo.controller;

import com.defense3.demo.common.Result;
import com.defense3.demo.dto.DepartmentRequest;
import com.defense3.demo.dto.DepartmentResponse;
import com.defense3.demo.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 院系管理控制器（超级管理员）
 */
@RestController
@RequestMapping("/api/admin/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 获取所有院系
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_ADMIN')")
    public Result<List<DepartmentResponse>> list() {
        return Result.success(departmentService.findAll());
    }

    /**
     * 根据ID获取院系
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_ADMIN')")
    public Result<DepartmentResponse> getById(@PathVariable Long id) {
        return Result.success(departmentService.findById(id));
    }

    /**
     * 创建院系
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<DepartmentResponse> create(@Valid @RequestBody DepartmentRequest request) {
        return Result.success(departmentService.create(request));
    }

    /**
     * 更新院系
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<DepartmentResponse> update(@PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {
        return Result.success(departmentService.update(id, request));
    }

    /**
     * 删除院系
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return Result.success();
    }

    /**
     * 设置院系管理员
     */
    @PostMapping("/{id}/admin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> setAdmin(@PathVariable Long id, @RequestBody java.util.Map<String, Long> body) {
        Long adminId = body.get("adminId");
        departmentService.setAdmin(id, adminId);
        return Result.success();
    }

    /**
     * 上传系主任签名
     */
    @PostMapping("/{id}/dean-signature")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<String> uploadDeanSignature(@PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {
        String path = departmentService.uploadDeanSignature(id, file);
        return Result.success(path);
    }

    /**
     * 创建院系管理员（新建账号）
     */
    @PostMapping("/{id}/create-admin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> createAdmin(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        String username = body.get("username");
        String name = body.get("name");
        String password = body.get("password");
        departmentService.createAdmin(id, username, name, password);
        return Result.success();
    }
}
