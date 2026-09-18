package com.defense3.demo.service;

import com.defense3.demo.entity.ScoreConfig;
import com.defense3.demo.entity.ThesisType;
import com.defense3.demo.repository.ScoreConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 评分配置服务
 */
@Service
@RequiredArgsConstructor
public class ScoreConfigService {

    private final ScoreConfigRepository scoreConfigRepository;

    /**
     * 根据论文类型获取评分配置
     */
    public List<ScoreConfig> findByThesisType(ThesisType thesisType) {
        return scoreConfigRepository.findByThesisTypeOrderByItemIndex(thesisType);
    }

    /**
     * 获取所有评分配置
     */
    public List<ScoreConfig> findAll() {
        return scoreConfigRepository.findAll();
    }

    /**
     * 批量保存评分配置（先删后插）
     */
    @Transactional
    public List<ScoreConfig> saveAll(ThesisType thesisType, List<ScoreConfig> configs) {
        // 删除该类型的所有旧配置
        scoreConfigRepository.deleteByThesisType(thesisType);

        // 设置类型和序号
        for (int i = 0; i < configs.size(); i++) {
            ScoreConfig config = configs.get(i);
            config.setId(null); // 确保新建
            config.setThesisType(thesisType);
            config.setItemIndex(i + 1);
        }

        return scoreConfigRepository.saveAll(configs);
    }

    /**
     * 更新单个配置
     */
    public ScoreConfig update(Long id, ScoreConfig config) {
        ScoreConfig existing = scoreConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("配置不存在"));

        existing.setItemName(config.getItemName());
        existing.setItemDescription(config.getItemDescription());
        existing.setWeight(config.getWeight());
        existing.setMaxScore(config.getMaxScore());

        return scoreConfigRepository.save(existing);
    }
}
