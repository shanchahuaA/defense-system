package com.defense3.demo.service;

import com.defense3.demo.entity.DefenseYear;
import com.defense3.demo.exception.BusinessException;
import com.defense3.demo.repository.DefenseYearRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 答辩年份服务
 */
@Service
@RequiredArgsConstructor
public class DefenseYearService {

    private final DefenseYearRepository yearRepository;

    /**
     * 获取所有年份
     */
    public List<DefenseYear> findAll() {
        return yearRepository.findAll();
    }

    /**
     * 获取当前年份
     */
    public DefenseYear findCurrent() {
        List<DefenseYear> currentYears = yearRepository.findByIsCurrent(true);
        if (currentYears.isEmpty()) {
            throw new BusinessException("当前年份未设置");
        }
        return currentYears.get(0);
    }

    /**
     * 根据ID获取年份
     */
    public DefenseYear findById(Long id) {
        return yearRepository.findById(id)
                .orElseThrow(() -> new BusinessException("年份不存在"));
    }

    /**
     * 创建年份
     */
    @Transactional
    public DefenseYear create(String name, Boolean isCurrent, LocalDate scoreDate, LocalDate evaluationDate) {
        if (yearRepository.existsByName(name)) {
            throw new BusinessException("年份名称已存在");
        }

        // 如果设置为当前年份，取消其他年份的当前状态
        if (Boolean.TRUE.equals(isCurrent)) {
            yearRepository.findByIsCurrent(true).forEach(year -> {
                year.setIsCurrent(false);
                yearRepository.save(year);
            });
        }

        DefenseYear year = new DefenseYear();
        year.setName(name);
        year.setIsCurrent(isCurrent != null ? isCurrent : false);
        year.setScoreDate(scoreDate);
        year.setEvaluationDate(evaluationDate);

        return yearRepository.save(year);
    }

    /**
     * 更新年份
     */
    @Transactional
    public DefenseYear update(Long id, String name, Boolean isCurrent, LocalDate scoreDate, LocalDate evaluationDate) {
        DefenseYear year = yearRepository.findById(id)
                .orElseThrow(() -> new BusinessException("年份不存在"));

        // 检查名称是否重复
        if (!year.getName().equals(name) && yearRepository.existsByName(name)) {
            throw new BusinessException("年份名称已存在");
        }

        // 如果设置为当前年份，取消其他年份的当前状态
        if (Boolean.TRUE.equals(isCurrent) && !Boolean.TRUE.equals(year.getIsCurrent())) {
            yearRepository.findByIsCurrent(true).forEach(y -> {
                y.setIsCurrent(false);
                yearRepository.save(y);
            });
        }

        year.setName(name);
        year.setIsCurrent(isCurrent != null ? isCurrent : year.getIsCurrent());
        year.setScoreDate(scoreDate);
        year.setEvaluationDate(evaluationDate);

        return yearRepository.save(year);
    }

    /**
     * 删除年份
     */
    @Transactional
    public void delete(Long id) {
        DefenseYear year = yearRepository.findById(id)
                .orElseThrow(() -> new BusinessException("年份不存在"));

        if (Boolean.TRUE.equals(year.getIsCurrent())) {
            throw new BusinessException("无法删除当前年份");
        }

        yearRepository.deleteById(id);
    }

    /**
     * 设置当前年份
     */
    @Transactional
    public void setCurrent(Long id) {
        DefenseYear year = yearRepository.findById(id)
                .orElseThrow(() -> new BusinessException("年份不存在"));

        // 取消其他年份的当前状态
        yearRepository.findByIsCurrent(true).forEach(y -> {
            y.setIsCurrent(false);
            yearRepository.save(y);
        });

        year.setIsCurrent(true);
        yearRepository.save(year);
    }
}
