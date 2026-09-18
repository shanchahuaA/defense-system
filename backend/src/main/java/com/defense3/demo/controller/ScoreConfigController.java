package com.defense3.demo.controller;

import com.defense3.demo.common.Result;
import com.defense3.demo.entity.ScoreConfig;
import com.defense3.demo.entity.ThesisType;
import com.defense3.demo.service.ScoreConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评分配置控制器
 */
@RestController
@RequestMapping("/api/score-configs")
@RequiredArgsConstructor
public class ScoreConfigController {

    private final ScoreConfigService scoreConfigService;

    /**
     * 获取所有配置
     */
    @GetMapping
    public Result<List<ScoreConfig>> getAll() {
        return Result.success(scoreConfigService.findAll());
    }

    /**
     * 按论文类型获取配置
     */
    @GetMapping("/type/{thesisType}")
    public Result<List<ScoreConfig>> getByType(@PathVariable String thesisType) {
        ThesisType type = ThesisType.valueOf(thesisType.toUpperCase());
        return Result.success(scoreConfigService.findByThesisType(type));
    }

    /**
     * 批量保存配置（按类型）
     */
    @PostMapping("/type/{thesisType}")
    public Result<List<ScoreConfig>> saveByType(
            @PathVariable String thesisType,
            @RequestBody List<ScoreConfig> configs) {
        ThesisType type = ThesisType.valueOf(thesisType.toUpperCase());
        return Result.success(scoreConfigService.saveAll(type, configs));
    }

    /**
     * 更新单个配置
     */
    @PutMapping("/{id}")
    public Result<ScoreConfig> update(
            @PathVariable Long id,
            @RequestBody ScoreConfig config) {
        return Result.success(scoreConfigService.update(id, config));
    }
}
