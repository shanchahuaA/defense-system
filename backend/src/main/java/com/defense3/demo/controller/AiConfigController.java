package com.defense3.demo.controller;

import com.defense3.demo.common.Result;
import com.defense3.demo.entity.AiConfig;
import com.defense3.demo.service.AiConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * AI配置管理控制器
 */
@RestController
@RequestMapping("/api/admin/ai-config")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AiConfigController {

    private final AiConfigService aiConfigService;

    /**
     * 获取AI配置
     */
    @GetMapping
    public Result<AiConfig> getConfig() {
        return Result.success(aiConfigService.getConfig());
    }

    /**
     * 保存AI配置
     */
    @PostMapping
    public Result<AiConfig> saveConfig(@RequestBody AiConfig config) {
        return Result.success(aiConfigService.saveConfig(config));
    }

    /**
     * 测试API连接
     */
    @PostMapping("/test")
    public Result<String> testConnection(@RequestBody AiConfig config) {
        String result = aiConfigService.testConnection(config.getApiKey());
        return Result.success(result);
    }
}
