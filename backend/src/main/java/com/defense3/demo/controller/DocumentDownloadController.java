package com.defense3.demo.controller;

import com.defense3.demo.service.DocumentGenerationService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Slf4j
@RestController
// @RequestMapping("/api/documents") // 功能暂时停用
@RequiredArgsConstructor
public class DocumentDownloadController {

    private final DocumentGenerationService generationService;

    /**
     * 测试生成论文成绩表
     * 用户上传一个临时模板，系统用Mock数据填充后直接返回生成的Word
     */
    @PostMapping("/test-generate")
    public void testGenerate(@RequestParam("file") MultipartFile file, HttpServletResponse response) {
        Path tempTemplate = null;
        Path resultPath = null;
        try {
            // 1. 保存上传的测试模板
            tempTemplate = Files.createTempFile("template_", ".docx");
            Files.copy(file.getInputStream(), tempTemplate, StandardCopyOption.REPLACE_EXISTING);

            // 2. 生成文档
            String outputDir = "uploads/temp";
            resultPath = generationService.generateTestPaperScoreSheet(outputDir, tempTemplate.toString());

            // 3. 下载响应
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            String filename = URLEncoder.encode("生成测试_论文成绩表.docx", StandardCharsets.UTF_8.toString());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            try (OutputStream out = response.getOutputStream()) {
                Files.copy(resultPath, out);
            }

        } catch (Exception e) {
            log.error("生成测试文档失败", e);
            try {
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write("{\"code\":500, \"msg\":\"生成失败: " + e.getMessage() + "\"}");
            } catch (IOException ex) {
                // ignore
            }
        } finally {
            // 清理临时文件
            try {
                if (tempTemplate != null)
                    Files.deleteIfExists(tempTemplate);
                if (resultPath != null)
                    Files.deleteIfExists(resultPath);
            } catch (IOException e) {
                log.warn("清理临时文件失败", e);
            }
        }
    }
}
