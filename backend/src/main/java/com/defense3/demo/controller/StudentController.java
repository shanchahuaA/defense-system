package com.defense3.demo.controller;

import com.defense3.demo.common.PageResult;
import com.defense3.demo.common.Result;
import com.defense3.demo.dto.StudentRequest;
import com.defense3.demo.dto.StudentResponse;
import com.defense3.demo.entity.ThesisType;
import com.defense3.demo.security.CustomUserDetails;
import com.defense3.demo.service.AuthService;
import com.defense3.demo.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生管理控制器
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final AuthService authService;

    /**
     * 分页获取学生列表
     */
    @GetMapping("/dept/students")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_ADMIN')")
    public Result<PageResult<StudentResponse>> listStudents(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) Long yearId,
            @RequestParam(required = false) ThesisType thesisType,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<StudentResponse> page = studentService.findAll(departmentId, groupId, yearId, thesisType, pageable);
        return Result.success(PageResult.of(page));
    }

    /**
     * 获取我指导的学生
     */
    @GetMapping("/teacher/my-students")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_ADMIN', 'SUPER_ADMIN')")
    public Result<List<StudentResponse>> getMyStudents() {
        CustomUserDetails user = authService.getCurrentUser();
        return Result.success(studentService.findByAdvisor(user.getId()));
    }

    /**
     * 获取答辩小组的学生列表
     */
    @GetMapping("/groups/{groupId}/students")
    public Result<List<StudentResponse>> getGroupStudents(@PathVariable Long groupId) {
        return Result.success(studentService.findByGroupOrderByDefenseOrder(groupId));
    }

    /**
     * 根据ID获取学生
     */
    @GetMapping("/dept/students/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_ADMIN')")
    public Result<StudentResponse> getStudentById(@PathVariable Long id) {
        return Result.success(studentService.findById(id));
    }

    /**
     * 创建学生
     */
    @PostMapping("/dept/students")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_ADMIN')")
    public Result<StudentResponse> createStudent(@Valid @RequestBody StudentRequest request,
            @RequestHeader(value = "X-Year-Id", required = false) Long yearId) {
        return Result.success(studentService.create(request, yearId));
    }

    /**
     * 更新学生
     */
    @PutMapping("/dept/students/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_ADMIN', 'TEACHER')")
    public Result<StudentResponse> updateStudent(@PathVariable Long id,
            @Valid @RequestBody StudentRequest request) {
        return Result.success(studentService.update(id, request));
    }

    /**
     * 删除学生
     */
    @DeleteMapping("/dept/students/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_ADMIN')")
    public Result<Void> deleteStudent(@PathVariable Long id) {
        studentService.delete(id);
        return Result.success();
    }

    /**
     * 分配答辩小组
     */
    @PostMapping("/dept/students/{id}/assign-group")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_ADMIN')")
    public Result<Void> assignGroup(@PathVariable Long id,
            @RequestParam Long groupId,
            @RequestParam(required = false) Integer defenseOrder) {
        studentService.assignGroup(id, groupId, defenseOrder);
        return Result.success();
    }

    /**
     * 分配指导教师
     */
    @PostMapping("/dept/students/{id}/assign-advisor")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_ADMIN')")
    public Result<Void> assignAdvisor(@PathVariable Long id, @RequestParam Long advisorId) {
        studentService.assignAdvisor(id, advisorId);
        return Result.success();
    }

    /**
     * 分配评阅人
     */
    @PostMapping("/dept/students/{id}/assign-reviewer")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_ADMIN', 'TEACHER')")
    public Result<Void> assignReviewer(@PathVariable Long id, @RequestParam Long reviewerId) {
        studentService.assignReviewer(id, reviewerId);
        return Result.success();
    }
}
