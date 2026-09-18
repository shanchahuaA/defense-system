package com.defense3.demo.service;

import com.defense3.demo.exception.BusinessException;
import com.defense3.demo.entity.AiConfig;
import com.defense3.demo.entity.Student;
import com.defense3.demo.repository.AiConfigRepository;
import com.defense3.demo.repository.StudentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * AI评语生成服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final AiConfigRepository aiConfigRepository;
    private final StudentRepository studentRepository;
    private final ObjectMapper objectMapper;

    private static final String AI_API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    /**
     * 为学生生成AI评语
     */
    @Transactional
    public String generateComment(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("学生不存在"));

        AiConfig config = aiConfigRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new BusinessException("请先配置AI参数"));

        if (config.getApiKey() == null || config.getApiKey().isBlank()) {
            throw new BusinessException("AI API Key未配置");
        }

        // 根据论文类型选择prompt
        String promptTemplate = "PAPER".equals(student.getThesisType())
                ? config.getPaperPrompt()
                : config.getDesignPrompt();

        if (promptTemplate == null || promptTemplate.isBlank()) {
            throw new BusinessException("AI评语模板未配置");
        }

        // 构建评语生成的上下文
        String prompt = buildPrompt(promptTemplate, student);

        // 调用AI接口
        String comment = callAiApi(config.getApiKey(), prompt);

        // 保存评语到学生记录
        student.setAiComment(comment);
        studentRepository.save(student);

        return comment;
    }

    /**
     * 批量生成评语
     */
    @Transactional
    public Map<Long, String> batchGenerate(List<Long> studentIds) {
        Map<Long, String> results = new HashMap<>();
        for (Long studentId : studentIds) {
            try {
                String comment = generateComment(studentId);
                results.put(studentId, comment);
            } catch (Exception e) {
                log.error("生成评语失败，学生ID: {}", studentId, e);
                results.put(studentId, "生成失败: " + e.getMessage());
            }
        }
        return results;
    }

    /**
     * 保存评语（手动编辑后保存）
     */
    @Transactional
    public void saveComment(Long studentId, String comment) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("学生不存在"));
        student.setAiComment(comment);
        studentRepository.save(student);
    }

    /**
     * 构建prompt
     */
    private String buildPrompt(String template, Student student) {
        // 替换模板中的变量
        String prompt = template
                .replace("{studentName}", student.getName() != null ? student.getName() : "")
                .replace("{thesisTitle}", student.getThesisTitle() != null ? student.getThesisTitle() : "")
                .replace("{thesisType}", "PAPER".equals(student.getThesisType()) ? "毕业论文" : "毕业设计");

        // 添加评分信息（如果有）
        StringBuilder scoreInfo = new StringBuilder();
        if (student.getAdvisorScore() != null) {
            scoreInfo.append("指导教师评分: ").append(student.getAdvisorScore()).append("分; ");
        }
        if (student.getReviewerScore() != null) {
            scoreInfo.append("评阅人评分: ").append(student.getReviewerScore()).append("分; ");
        }

        if (scoreInfo.length() > 0) {
            prompt = prompt.replace("{scores}", scoreInfo.toString());
        } else {
            prompt = prompt.replace("{scores}", "暂无成绩");
        }

        return prompt;
    }

    /**
     * 调用AI API
     */
    private String callAiApi(String apiKey, String prompt) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "qwen-plus");
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", "你是一个专业的毕业答辩评委，请根据学生信息生成专业、客观的答辩评语。"),
                    Map.of("role", "user", "content", prompt)));
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 500);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    AI_API_URL,
                    HttpMethod.POST,
                    request,
                    String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode choices = root.path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    return choices.get(0).path("message").path("content").asText();
                }
            }

            throw new BusinessException("AI接口返回异常");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用AI接口失败", e);
            throw new BusinessException("AI服务调用失败: " + e.getMessage());
        }
    }
}
