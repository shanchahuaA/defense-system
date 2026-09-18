package com.defense3.demo.controller;

import com.defense3.demo.common.Result;
import com.defense3.demo.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI评语控制器
 */
@RestController
@RequestMapping("/api/teacher/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /**
     * 为单个学生生成AI评语
     */
    @PostMapping("/generate/{studentId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_ADMIN', 'SUPER_ADMIN')")
    public Result<String> generateComment(@PathVariable Long studentId) {
        String comment = aiService.generateComment(studentId);
        return Result.success(comment);
    }

    /**
     * 批量生成AI评语
     */
    @PostMapping("/batch-generate")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_ADMIN', 'SUPER_ADMIN')")
    public Result<Map<Long, String>> batchGenerate(@RequestBody List<Long> studentIds) {
        Map<Long, String> results = aiService.batchGenerate(studentIds);
        return Result.success(results);
    }

    /**
     * 保存评语（手动编辑后）
     */
    @PostMapping("/save/{studentId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'DEPT_ADMIN', 'SUPER_ADMIN')")
    public Result<Void> saveComment(@PathVariable Long studentId, @RequestBody Map<String, String> body) {
        String comment = body.get("comment");
        aiService.saveComment(studentId, comment);
        return Result.success();
    }
}
