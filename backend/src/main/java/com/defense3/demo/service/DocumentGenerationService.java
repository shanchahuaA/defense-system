package com.defense3.demo.service;

import com.deepoove.poi.XWPFTemplate;
import com.defense3.demo.entity.Student;
import com.defense3.demo.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentGenerationService {

    private final StudentRepository studentRepository;
    private final DocumentTemplateService templateService;

    /**
     * 生成论文成绩表
     */
    public Path generatePaperScoreSheet(Long groupId, String outputDir) throws IOException {
        // 1. 获取数据
        List<Student> students = studentRepository.findByGroupIdOrderByDefenseOrder(groupId);
        // 如果没有真实数据，为了演示，我们可以 Mock 一些
        // 但正式代码不应包含 Mock 逻辑。这里我们假设数据已存在。
        // 为了响应用户 "提供测试数据" 的请求，我们可以在 Controller 层做 Mock。

        if (students.isEmpty()) {
            throw new RuntimeException("该小组没有学生");
        }

        Student firstStudent = students.get(0);
        String deptName = firstStudent.getDepartment() != null ? firstStudent.getDepartment().getName() : "计算机学院";
        String groupName = firstStudent.getGroup() != null ? firstStudent.getGroup().getName() : "第一答辩小组";
        String location = firstStudent.getDefenseLocation() != null ? firstStudent.getDefenseLocation() : "302教室";
        LocalDate date = LocalDate.now();

        // 2. 组装数据
        Map<String, Object> data = new HashMap<>();
        data.put("deptName", deptName);
        data.put("groupIndex", groupName);
        data.put("year", String.valueOf(date.getYear()));
        data.put("month", String.valueOf(date.getMonthValue()));
        data.put("day", String.valueOf(date.getDayOfMonth()));
        data.put("location", location);
        data.put("judgeName", "张老师");

        List<Map<String, Object>> studentList = new ArrayList<>();
        for (Student s : students) {
            Map<String, Object> map = new HashMap<>();
            map.put("studentNo", s.getStudentNo());
            map.put("name", s.getName());
            map.put("thesisTitle", s.getThesisTitle());
            map.put("score1", s.getFinalScore() != null ? s.getFinalScore().toString() : "85");
            map.put("totalScore", s.getFinalScore() != null ? s.getFinalScore().toString() : "85");
            studentList.add(map);
        }
        data.put("students", studentList);

        // 3. 读取模板
        String templatePath = templateService.getTemplatePath("PAPER_DEFENSE_SCORE");
        if (templatePath == null) {
            // 回退机制：如果数据库没配置，尝试读取本地默认路径（用于测试）
            templatePath = "uploads/templates/论文成绩表.docx";
            if (!Files.exists(Paths.get(templatePath))) {
                throw new RuntimeException("未找到论文成绩表模板，请先在模板管理中上传");
            }
        }

        // 5. 生成文件
        try (XWPFTemplate template = XWPFTemplate.compile(templatePath).render(data)) {
            Path outputPath = Paths.get(outputDir, "论文成绩表_" + groupId + "_" + System.currentTimeMillis() + ".docx");
            Files.createDirectories(outputPath.getParent());

            template.writeToFile(outputPath.toString());
            return outputPath;
        }
    }

    /**
     * 生成测试用的论文成绩表 (使用Mock数据)
     */
    public Path generateTestPaperScoreSheet(String outputDir, String templatePath) throws IOException {
        // Mock 数据
        Map<String, Object> data = new HashMap<>();
        data.put("deptName", "软件学院");
        data.put("groupIndex", "测试小组");
        data.put("year", "2025");
        data.put("month", "6");
        data.put("day", "15");
        data.put("location", "综合楼501");
        data.put("judgeName", "王教授");

        List<Map<String, Object>> studentList = new ArrayList<>();
        // 添加3个测试学生
        studentList.add(Map.of("studentNo", "2021001", "name", "张三", "thesisTitle", "基于Vue的教务系统", "score1", "88",
                "totalScore", "88"));
        studentList.add(Map.of("studentNo", "2021002", "name", "李四", "thesisTitle", "Spring Cloud 微服务实践", "score1",
                "92", "totalScore", "92"));
        studentList.add(Map.of("studentNo", "2021003", "name", "王五", "thesisTitle", "深度学习图像识别", "score1", "85",
                "totalScore", "85"));

        // 5. 生成文件
        // 移除 RenderPolicy，允许用户使用原生区块语法 {{#students}} ... {{/students}}
        try (XWPFTemplate template = XWPFTemplate.compile(templatePath).render(data)) {
            Path outputPath = Paths.get(outputDir, "测试_论文成绩表.docx");
            Files.createDirectories(outputPath.getParent());
            template.writeToFile(outputPath.toString());
            return outputPath;
        }
    }
}
