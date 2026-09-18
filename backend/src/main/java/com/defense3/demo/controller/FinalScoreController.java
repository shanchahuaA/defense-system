package com.defense3.demo.controller;

import com.defense3.demo.common.Result;
import com.defense3.demo.dto.FinalScoreRequest;
import com.defense3.demo.dto.FinalScoreResponse;
import com.defense3.demo.entity.FinalScore;
import com.defense3.demo.security.CustomUserDetails;
import com.defense3.demo.service.AuthService;
import com.defense3.demo.service.FinalScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 大组答辩评分控制器
 */
@RestController
@RequestMapping("/api/teacher/final-score")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER', 'DEPT_ADMIN', 'SUPER_ADMIN')")
public class FinalScoreController {

    private final FinalScoreService finalScoreService;
    private final AuthService authService;

    /**
     * 获取需要参加大组答辩的学生（每个小组第一名）
     */
    @GetMapping("/students")
    public Result<List<FinalScoreResponse>> getFinalDefenseStudents(
            @RequestParam(required = false) Long yearId) {
        return Result.success(finalScoreService.getFinalDefenseStudents(yearId));
    }

    /**
     * 保存大组答辩评分
     */
    @PostMapping
    public Result<FinalScore> saveScore(@RequestBody FinalScoreRequest request) {
        CustomUserDetails user = authService.getCurrentUser();
        request.setTeacherId(user.getId());
        return Result.success(finalScoreService.saveScore(request));
    }

    /**
     * 获取当前教师对某学生的评分
     */
    @GetMapping("/{studentId}")
    public Result<FinalScore> getMyScore(
            @PathVariable Long studentId,
            @RequestParam(required = false) Long yearId) {
        CustomUserDetails user = authService.getCurrentUser();
        return Result.success(finalScoreService.getTeacherStudentScore(user.getId(), studentId, yearId));
    }
}
