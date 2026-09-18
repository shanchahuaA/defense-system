package com.defense3.demo.controller;

import com.defense3.demo.common.Result;
import com.defense3.demo.entity.DocumentTemplate;
import com.defense3.demo.service.DocumentTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 文档模板管理控制器
 */
@RestController
@RequestMapping("/api/admin/templates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class DocumentTemplateController {

    private final DocumentTemplateService templateService;

    /**
     * 获取所有模板
     */
    @GetMapping
    public Result<List<DocumentTemplate>> getAllTemplates() {
        return Result.success(templateService.getAllTemplates());
    }

    /**
     * 根据类型获取模板
     */
    @GetMapping("/{templateType}")
    public Result<DocumentTemplate> getByType(@PathVariable String templateType) {
        return Result.success(templateService.getByType(templateType));
    }

    /**
     * 上传模板
     */
    @PostMapping("/upload")
    public Result<DocumentTemplate> uploadTemplate(
            @RequestParam String templateType,
            @RequestParam String templateName,
            @RequestParam("file") MultipartFile file) throws IOException {
        return Result.success(templateService.uploadTemplate(templateType, templateName, file));
    }

    /**
     * 删除模板
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return Result.success();
    }

    /**
     * 获取模板类型列表
     */
    @GetMapping("/types")
    public Result<Map<String, String>> getTemplateTypes() {
        return Result.success(Map.of(
                "PAPER_DEFENSE_SCORE", "论文答辩成绩表",
                "DESIGN_DEFENSE_SCORE", "设计答辩成绩表",
                "PAPER_EVALUATION", "论文成绩评定表",
                "DESIGN_EVALUATION", "设计成绩评定表",
                "GROUP_STATISTICS", "答辩小组统分表",
                "PAPER_SCORE_PROCESS", "论文答辩成绩无评语过程表",
                "DESIGN_SCORE_PROCESS", "设计答辩成绩无评语过程表"));
    }
}
