package com.defense3.demo.controller;

import com.defense3.demo.common.Result;
import com.defense3.demo.entity.DefenseYear;
import com.defense3.demo.service.DefenseYearService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

/**
 * 年份管理控制器
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DefenseYearController {

    private final DefenseYearService yearService;

    /**
     * 获取所有年份
     */
    @GetMapping("/public/years")
    public Result<List<DefenseYear>> listYears() {
        return Result.success(yearService.findAll());
    }

    /**
     * 获取当前年份
     */
    @GetMapping("/public/years/current")
    public Result<DefenseYear> getCurrentYear() {
        return Result.success(yearService.findCurrent());
    }

    /**
     * 根据ID获取年份
     */
    @GetMapping("/admin/years/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<DefenseYear> getYearById(@PathVariable Long id) {
        return Result.success(yearService.findById(id));
    }

    /**
     * 创建年份
     */
    @PostMapping("/admin/years")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<DefenseYear> createYear(@RequestParam String name,
            @RequestParam(defaultValue = "false") Boolean isCurrent,
            @RequestParam(required = false) String scoreDate,
            @RequestParam(required = false) String evaluationDate) {
        LocalDate parsedScoreDate = scoreDate != null ? LocalDate.parse(scoreDate) : null;
        LocalDate parsedEvalDate = evaluationDate != null ? LocalDate.parse(evaluationDate) : null;
        return Result.success(yearService.create(name, isCurrent, parsedScoreDate, parsedEvalDate));
    }

    /**
     * 更新年份
     */
    @PutMapping("/admin/years/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<DefenseYear> updateYear(@PathVariable Long id,
            @RequestParam String name,
            @RequestParam(required = false) Boolean isCurrent,
            @RequestParam(required = false) String scoreDate,
            @RequestParam(required = false) String evaluationDate) {
        LocalDate parsedScoreDate = scoreDate != null ? LocalDate.parse(scoreDate) : null;
        LocalDate parsedEvalDate = evaluationDate != null ? LocalDate.parse(evaluationDate) : null;
        return Result.success(yearService.update(id, name, isCurrent, parsedScoreDate, parsedEvalDate));
    }

    /**
     * 删除年份
     */
    @DeleteMapping("/admin/years/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> deleteYear(@PathVariable Long id) {
        yearService.delete(id);
        return Result.success();
    }

    /**
     * 设置当前年份
     */
    @PostMapping("/admin/years/{id}/set-current")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> setCurrent(@PathVariable Long id) {
        yearService.setCurrent(id);
        return Result.success();
    }
}
