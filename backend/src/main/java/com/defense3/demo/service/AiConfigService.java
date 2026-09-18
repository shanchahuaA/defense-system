package com.defense3.demo.service;

import com.defense3.demo.entity.AiConfig;
import com.defense3.demo.repository.AiConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * AI配置服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiConfigService {

    private final AiConfigRepository aiConfigRepository;

    private static final String AI_API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    /**
     * 获取AI配置（只存一条记录）
     */
    /**
     * 获取AI配置（只存一条记录）
     */
    public AiConfig getConfig() {
        AiConfig config = aiConfigRepository.findTopByOrderByIdAsc();
        if (config == null) {
            return createDefaultConfig();
        }
        return config;
    }

    /**
     * 创建默认配置
     */
    private AiConfig createDefaultConfig() {
        AiConfig config = new AiConfig();
        config.setPaperPrompt(getDefaultPaperPrompt());
        config.setDesignPrompt(getDefaultDesignPrompt());
        return aiConfigRepository.save(config);
    }

    /**
     * 保存配置
     */
    @Transactional
    public AiConfig saveConfig(AiConfig newConfig) {
        AiConfig config = getConfig();
        config.setApiKey(newConfig.getApiKey());
        config.setPaperPrompt(newConfig.getPaperPrompt());
        config.setDesignPrompt(newConfig.getDesignPrompt());
        return aiConfigRepository.save(config);
    }

    /**
     * 测试API连接
     */
    public String testConnection(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return "API Key不能为空";
        }

        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = Map.of(
                    "model", "qwen-plus",
                    "messages", List.of(
                            Map.of("role", "user", "content", "你好，请回复'连接成功'")),
                    "max_tokens", 20);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("null")
            ResponseEntity<String> response = restTemplate.exchange(
                    AI_API_URL,
                    HttpMethod.POST,
                    request,
                    String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return "连接成功！API服务正常";
            } else {
                return "连接失败：" + response.getStatusCode();
            }
        } catch (Exception e) {
            log.error("测试AI连接失败", e);
            return "连接失败：" + e.getMessage();
        }
    }

    /**
     * 默认论文评语提示词
     */
    private String getDefaultPaperPrompt() {
        return """
                # Role
                你是一名资深的高校毕业答辩委员会专家，拥有丰富的学术评审经验。你的语气专业、客观、严谨。

                # Context
                根据以下学生信息，生成一段用于正式归档的本科毕业论文答辩评语。

                # Input Data
                学生姓名：{studentName}
                论文题目：{thesisTitle}
                成绩参考：{scores}

                # Constraints
                1. 语言必须学术化，严禁出现"作为AI"等字眼。
                2. 字数控制在 100-150 字。
                3. 评价维度：选题意义、论文结构与质量、答辩陈述表现、回答问题情况。
                4. 结论明确（如"同意通过答辩"）。

                # Example
                该生论文选题切合实际，工作量适中。论文结构完整，逻辑清晰，数据详实。答辩过程中陈述流畅，重点突出。回答问题时概念清楚，基本能通过理论分析解决实际问题。论文达到本科毕业标准，同意通过答辩。

                # Output
                请直接输出评语内容，不要包含任何前缀。""";
    }

    /**
     * 默认设计评语提示词
     */
    private String getDefaultDesignPrompt() {
        return """
                # Role
                你是一名资深的高校毕业答辩委员会专家，拥有丰富的学术评审经验。

                # Context
                根据以下学生信息，生成一段用于正式归档的本科毕业设计答辩评语。

                # Input Data
                学生姓名：{studentName}
                设计题目：{thesisTitle}
                成绩参考：{scores}

                # Constraints
                1. 语言必须学术化，书面化。
                2. 字数控制在 100-150 字。
                3. 评价维度：设计方案合理性、功能实现完成度、答辩演示效果、回答问题准确性。
                4. 结论明确。

                # Example
                该生设计方案合理，技术路线可行，完成了任务书规定的各项任务。系统运行稳定，功能满足要求。答辩演示流畅，回答问题准确，对相关技术掌握较好。设计达到本科毕业标准，同意通过答辩。

                # Output
                请直接输出评语内容，不要包含任何前缀。""";
    }
}
