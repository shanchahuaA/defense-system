package com.defense3.demo.service;

import com.defense3.demo.entity.GroupScore;
import com.defense3.demo.repository.GroupScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 小组评分服务
 */
@Service
@RequiredArgsConstructor
public class GroupScoreService {

    private final GroupScoreRepository groupScoreRepository;

    /**
     * 保存或更新评分
     */
    @Transactional
    public GroupScore saveOrUpdate(GroupScore score) {
        // 计算总分
        int total = (score.getItem1Score() != null ? score.getItem1Score() : 0)
                + (score.getItem2Score() != null ? score.getItem2Score() : 0)
                + (score.getItem3Score() != null ? score.getItem3Score() : 0)
                + (score.getItem4Score() != null ? score.getItem4Score() : 0)
                + (score.getItem5Score() != null ? score.getItem5Score() : 0)
                + (score.getItem6Score() != null ? score.getItem6Score() : 0);
        score.setTotalScore(total);

        // 检查是否已存在评分记录
        Optional<GroupScore> existing = groupScoreRepository
                .findByStudentIdAndTeacherIdAndYearId(
                        score.getStudentId(),
                        score.getTeacherId(),
                        score.getYearId());

        if (existing.isPresent()) {
            GroupScore old = existing.get();
            old.setItem1Score(score.getItem1Score());
            old.setItem2Score(score.getItem2Score());
            old.setItem3Score(score.getItem3Score());
            old.setItem4Score(score.getItem4Score());
            old.setItem5Score(score.getItem5Score());
            old.setItem6Score(score.getItem6Score());
            old.setTotalScore(total);
            return groupScoreRepository.save(old);
        }

        return groupScoreRepository.save(score);
    }

    /**
     * 获取学生的所有评分记录
     */
    public List<GroupScore> findByStudentId(Long studentId) {
        return groupScoreRepository.findByStudentId(studentId);
    }

    /**
     * 获取学生在指定年份的评分记录
     */
    public List<GroupScore> findByStudentIdAndYearId(Long studentId, Long yearId) {
        return groupScoreRepository.findByStudentIdAndYearId(studentId, yearId);
    }

    /**
     * 获取教师对某学生的评分
     */
    public Optional<GroupScore> findByStudentAndTeacher(Long studentId, Long teacherId, Long yearId) {
        return groupScoreRepository.findByStudentIdAndTeacherIdAndYearId(studentId, teacherId, yearId);
    }

    /**
     * 获取教师在某年份的所有评分
     */
    public List<GroupScore> findByTeacherAndYear(Long teacherId, Long yearId) {
        return groupScoreRepository.findByTeacherIdAndYearId(teacherId, yearId);
    }

    /**
     * 获取学生的平均分
     */
    public Double getAverageScore(Long studentId, Long yearId) {
        return groupScoreRepository.getAverageScoreByStudentIdAndYearId(studentId, yearId);
    }

    /**
     * 检查教师是否已评分
     */
    public boolean hasScored(Long studentId, Long teacherId, Long yearId) {
        return groupScoreRepository.existsByStudentIdAndTeacherIdAndYearId(studentId, teacherId, yearId);
    }
}
