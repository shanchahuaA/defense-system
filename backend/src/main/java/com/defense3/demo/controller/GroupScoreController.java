package com.defense3.demo.controller;

import com.defense3.demo.common.Result;
import com.defense3.demo.entity.GroupScore;
import com.defense3.demo.service.GroupScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小组评分控制器
 */
@RestController
@RequestMapping("/api/group-scores")
@RequiredArgsConstructor
public class GroupScoreController {

    private final GroupScoreService groupScoreService;

    /**
     * 提交或更新评分
     */
    @PostMapping
    public Result<GroupScore> saveScore(@RequestBody GroupScore score) {
        return Result.success(groupScoreService.saveOrUpdate(score));
    }

    /**
     * 获取学生的所有评分
     */
    @GetMapping("/student/{studentId}")
    public Result<List<GroupScore>> getByStudent(@PathVariable Long studentId) {
        return Result.success(groupScoreService.findByStudentId(studentId));
    }

    /**
     * 获取学生在指定年份的评分
     */
    @GetMapping("/student/{studentId}/year/{yearId}")
    public Result<List<GroupScore>> getByStudentAndYear(
            @PathVariable Long studentId,
            @PathVariable Long yearId) {
        return Result.success(groupScoreService.findByStudentIdAndYearId(studentId, yearId));
    }

    /**
     * 获取教师对某学生的评分
     */
    @GetMapping("/teacher/{teacherId}/student/{studentId}/year/{yearId}")
    public Result<GroupScore> getByTeacherAndStudent(
            @PathVariable Long teacherId,
            @PathVariable Long studentId,
            @PathVariable Long yearId) {
        return Result.success(
                groupScoreService.findByStudentAndTeacher(studentId, teacherId, yearId)
                        .orElse(null));
    }

    /**
     * 获取教师在某年份的所有评分
     */
    @GetMapping("/teacher/{teacherId}/year/{yearId}")
    public Result<List<GroupScore>> getByTeacherAndYear(
            @PathVariable Long teacherId,
            @PathVariable Long yearId) {
        return Result.success(groupScoreService.findByTeacherAndYear(teacherId, yearId));
    }

    /**
     * 获取学生平均分
     */
    @GetMapping("/student/{studentId}/year/{yearId}/average")
    public Result<Double> getAverageScore(
            @PathVariable Long studentId,
            @PathVariable Long yearId) {
        return Result.success(groupScoreService.getAverageScore(studentId, yearId));
    }

    /**
     * 检查是否已评分
     */
    @GetMapping("/check")
    public Result<Boolean> checkScored(
            @RequestParam Long studentId,
            @RequestParam Long teacherId,
            @RequestParam Long yearId) {
        return Result.success(groupScoreService.hasScored(studentId, teacherId, yearId));
    }
}
