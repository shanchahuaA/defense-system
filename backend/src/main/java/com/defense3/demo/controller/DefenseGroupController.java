package com.defense3.demo.controller;

import com.defense3.demo.common.Result;
import com.defense3.demo.dto.DefenseGroupRequest;
import com.defense3.demo.dto.DefenseGroupResponse;
import com.defense3.demo.dto.StudentResponse;
import com.defense3.demo.security.CustomUserDetails;
import com.defense3.demo.service.AuthService;
import com.defense3.demo.service.DefenseGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 答辩小组管理控制器
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DefenseGroupController {

    private final DefenseGroupService groupService;
    private final AuthService authService;

    /**
     * 获取院系的答辩小组列表
     */
    @GetMapping("/dept/groups")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_ADMIN')")
    public Result<List<DefenseGroupResponse>> listGroups(
            @RequestParam Long departmentId,
            @RequestHeader(value = "X-Year-Id", required = false) Long yearId) {
        return Result.success(groupService.findByDepartmentAndYear(departmentId, yearId));
    }

    /**
     * 获取我所在的答辩小组
     */
    @GetMapping("/teacher/my-groups")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_ADMIN', 'SUPER_ADMIN')")
    public Result<List<DefenseGroupResponse>> getMyGroups(
            @RequestHeader(value = "X-Year-Id", required = false) Long yearId) {
        CustomUserDetails user = authService.getCurrentUser();
        return Result.success(groupService.findByTeacher(user.getId(), yearId));
    }

    /**
     * 获取我作为组长的小组学生列表
     */
    @GetMapping("/teacher/my-group-students")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_ADMIN', 'SUPER_ADMIN')")
    public Result<List<StudentResponse>> getMyGroupStudents(
            @RequestHeader(value = "X-Year-Id", required = false) Long yearId) {
        CustomUserDetails user = authService.getCurrentUser();
        return Result.success(groupService.findStudentsByLeader(user.getId(), yearId));
    }

    /**
     * 根据ID获取答辩小组
     */
    @GetMapping("/groups/{id}")
    public Result<DefenseGroupResponse> getGroupById(@PathVariable Long id) {
        return Result.success(groupService.findById(id));
    }

    /**
     * 创建答辩小组
     */
    @PostMapping("/dept/groups")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_ADMIN')")
    public Result<DefenseGroupResponse> createGroup(
            @Valid @RequestBody DefenseGroupRequest request,
            @RequestHeader(value = "X-Year-Id", required = false) Long yearId) {
        return Result.success(groupService.create(request, yearId));
    }

    /**
     * 更新答辩小组
     */
    @PutMapping("/dept/groups/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_ADMIN')")
    public Result<DefenseGroupResponse> updateGroup(@PathVariable Long id,
            @Valid @RequestBody DefenseGroupRequest request) {
        return Result.success(groupService.update(id, request));
    }

    /**
     * 删除答辩小组
     */
    @DeleteMapping("/dept/groups/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_ADMIN')")
    public Result<Void> deleteGroup(@PathVariable Long id) {
        groupService.delete(id);
        return Result.success();
    }

    /**
     * 开启大组答辩
     */
    @PostMapping("/dept/groups/start-university-defense")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_ADMIN')")
    public Result<Void> startUniversityDefense(
            @RequestParam Long departmentId,
            @RequestHeader(value = "X-Year-Id", required = false) Long yearId) {
        groupService.startUniversityDefense(departmentId, yearId);
        return Result.success();
    }
}
