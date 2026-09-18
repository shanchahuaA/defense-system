package com.defense3.demo.controller;

import com.defense3.demo.utils.TemplateGenerator;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
// @RequestMapping("/api/documents") // 功能暂时停用
public class TemplateDownloadController {

    /**
     * 下载标准、干净的 Word 模板
     * 用于解决用户手动编辑模板时出现的格式错误
     */
    @GetMapping("/download-standard-template")
    public void downloadStandardTemplate(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            String filename = URLEncoder.encode("标准论文成绩表模板.docx", StandardCharsets.UTF_8.toString());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            try (OutputStream out = response.getOutputStream()) {
                TemplateGenerator.createStandardTemplate(out);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 内部自测接口：生成模板 -> 渲染数据 -> 下载
     * 用于验证代码逻辑是否正确，排除用户上传文件的干扰
     */
    @GetMapping("/debug-generate")
    public void debugGenerate(HttpServletResponse response) {
        try {
            // 1. 生成临时模板文件
            java.nio.file.Path tempTpl = java.nio.file.Files.createTempFile("debug_tpl_", ".docx");
            try (java.io.OutputStream out = java.nio.file.Files.newOutputStream(tempTpl)) {
                TemplateGenerator.createStandardTemplate(out);
            }

            // 2. 调用生成服务进行渲染
            // 注意：这里需要传入 MultipartFile，但我们只有 File。
            // 为了简化，我们直接调用 generationService 的内部逻辑，或者重构 generationService 允许传入 Path。
            // 这里我们直接手动执行 generationService 的逻辑 demo

            // 准备数据
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("deptName", "计算机与信息学院");
            data.put("groupIndex", "1");
            data.put("year", "2024");
            data.put("month", "05");
            data.put("day", "20");
            data.put("location", "明理楼C301");
            data.put("judgeName", "张三");
            data.put("totalScore", "85"); // 这里应该是计算出来的，但测试先写死

            java.util.List<java.util.Map<String, Object>> students = new java.util.ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                java.util.Map<String, Object> s = new java.util.HashMap<>();
                s.put("studentNo", "2020xxx" + i);
                s.put("name", "学生" + i);
                s.put("thesisTitle", "论文题目" + i);
                s.put("score1", 40);
                s.put("score2", 20);
                s.put("score3", 25);
                s.put("totalScore", 85);
                students.add(s);
            }
            data.put("students", students);

            // 渲染
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            String filename = URLEncoder.encode("DEBUG_生成的文档.docx", StandardCharsets.UTF_8.toString());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            try (com.deepoove.poi.XWPFTemplate template = com.deepoove.poi.XWPFTemplate
                    .compile(tempTpl.toFile().getAbsolutePath()).render(data)) {
                template.write(response.getOutputStream());
            }

            // 清理
            java.nio.file.Files.deleteIfExists(tempTpl);

        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.getWriter().write("Error: " + e.getMessage());
            } catch (Exception ex) {
            }
        }
    }
}
