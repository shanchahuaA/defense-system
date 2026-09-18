package com.defense3.demo.service;

import com.defense3.demo.entity.DocumentTemplate;
import com.defense3.demo.exception.BusinessException;
import com.defense3.demo.repository.DocumentTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * 文档模板服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentTemplateService {

    private final DocumentTemplateRepository templateRepository;

    @Value("${file.upload.template-path:uploads/templates}")
    private String templatePath;

    /**
     * 获取所有模板
     */
    public List<DocumentTemplate> getAllTemplates() {
        return templateRepository.findAll();
    }

    /**
     * 根据类型获取模板
     */
    public DocumentTemplate getByType(String templateType) {
        return templateRepository.findByTemplateType(templateType).orElse(null);
    }

    /**
     * 获取模板路径
     */
    public String getTemplatePath(String templateType) {
        DocumentTemplate template = getByType(templateType);
        return template != null ? template.getFilePath() : null;
    }

    /**
     * 上传模板
     */
    @Transactional
    public DocumentTemplate uploadTemplate(String templateType, String templateName, MultipartFile file)
            throws IOException {
        // 验证文件
        if (file.isEmpty()) {
            throw new BusinessException("请选择文件");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.endsWith(".docx")) {
            throw new BusinessException("只支持 .docx 格式的 Word 文档");
        }

        // 确保目录存在
        Path uploadDir = Paths.get(templatePath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 生成新文件名
        String newFilename = templateType + "_" + UUID.randomUUID().toString().substring(0, 8) + ".docx";
        Path targetPath = uploadDir.resolve(newFilename);

        // 保存文件
        file.transferTo(targetPath);

        // 查找或创建模板记录
        DocumentTemplate template = templateRepository.findByTemplateType(templateType)
                .orElseGet(DocumentTemplate::new);

        // 删除旧文件
        if (template.getFilePath() != null) {
            try {
                Files.deleteIfExists(Paths.get(template.getFilePath()));
            } catch (IOException e) {
                log.warn("删除旧模板文件失败: {}", template.getFilePath());
            }
        }

        template.setTemplateType(templateType);
        template.setTemplateName(templateName);
        template.setFilePath(targetPath.toString());

        return templateRepository.save(template);
    }

    /**
     * 删除模板
     */
    @Transactional
    public void deleteTemplate(Long id) {
        DocumentTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("模板不存在"));

        // 删除文件
        if (template.getFilePath() != null) {
            try {
                Files.deleteIfExists(Paths.get(template.getFilePath()));
            } catch (IOException e) {
                log.warn("删除模板文件失败: {}", template.getFilePath());
            }
        }

        templateRepository.delete(template);
    }

    /**
     * 获取预定义的模板类型
     */
    public static final String[] TEMPLATE_TYPES = {
            "PAPER_DEFENSE_SCORE", // 论文答辩成绩表
            "DESIGN_DEFENSE_SCORE", // 设计答辩成绩表
            "PAPER_EVALUATION", // 论文成绩评定表
            "DESIGN_EVALUATION", // 设计成绩评定表
            "GROUP_STATISTICS", // 答辩小组统分表
            "PAPER_SCORE_PROCESS", // 论文答辩成绩无评语过程表
            "DESIGN_SCORE_PROCESS" // 设计答辩成绩无评语过程表
    };
}
