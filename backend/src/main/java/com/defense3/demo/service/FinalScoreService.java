package com.defense3.demo.service;

import com.defense3.demo.dto.FinalScoreRequest;
import com.defense3.demo.dto.FinalScoreResponse;
import com.defense3.demo.entity.FinalScore;
import com.defense3.demo.entity.Student;
import com.defense3.demo.exception.BusinessException;
import com.defense3.demo.repository.FinalScoreRepository;
import com.defense3.demo.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 大组答辩评分服务
 */
@Service
@RequiredArgsConstructor
public class FinalScoreService {

    private final FinalScoreRepository finalScoreRepository;
    private final StudentRepository studentRepository;

    /**
     * 获取需要参加大组答辩的学生（每个小组的第一名）
     */
    public List<FinalScoreResponse> getFinalDefenseStudents(Long yearId) {
        // 获取所有学生，按小组分组，找出每个小组的第一名
        List<Student> allStudents = studentRepository.findByYearId(yearId);

        // 按groupId分组，找出每组groupAvgScore最高的学生
        return allStudents.stream()
                .filter(s -> s.getGroupId() != null && s.getGroupAvgScore() != null)
                .collect(Collectors.groupingBy(Student::getGroupId))
                .values().stream()
                .map(groupStudents -> groupStudents.stream()
                        .max((a, b) -> a.getGroupAvgScore().compareTo(b.getGroupAvgScore()))
                        .orElse(null))
                .filter(s -> s != null)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 保存大组答辩评分
     */
    @Transactional
    public FinalScore saveScore(FinalScoreRequest request) {
        // 检查学生是否存在
        if (!studentRepository.existsById(request.getStudentId())) {
            throw new BusinessException("学生不存在");
        }

        // 查找或创建评分记录
        FinalScore score = finalScoreRepository
                .findByStudentIdAndTeacherIdAndYearId(
                        request.getStudentId(),
                        request.getTeacherId(),
                        request.getYearId())
                .orElseGet(FinalScore::new);

        score.setStudentId(request.getStudentId());
        score.setTeacherId(request.getTeacherId());
        score.setYearId(request.getYearId());
        score.setTotalScore(request.getTotalScore());

        FinalScore saved = finalScoreRepository.save(score);

        // 更新学生的大组答辩平均分
        updateStudentFinalScore(request.getStudentId(), request.getYearId());

        return saved;
    }

    /**
     * 获取教师对学生的评分
     */
    public FinalScore getTeacherStudentScore(Long teacherId, Long studentId, Long yearId) {
        return finalScoreRepository
                .findByStudentIdAndTeacherIdAndYearId(studentId, teacherId, yearId)
                .orElse(null);
    }

    /**
     * 更新学生的大组答辩最终成绩
     */
    private void updateStudentFinalScore(Long studentId, Long yearId) {
        Double avgScore = finalScoreRepository.getAverageScoreByStudentIdAndYearId(studentId, yearId);
        if (avgScore != null) {
            Student student = studentRepository.findById(studentId).orElse(null);
            if (student != null) {
                student.setFinalScore(BigDecimal.valueOf(avgScore).setScale(1, RoundingMode.HALF_UP));

                // 计算调节系数（大组成绩/小组成绩）
                if (student.getGroupAvgScore() != null &&
                        student.getGroupAvgScore().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal coefficient = BigDecimal.valueOf(avgScore)
                            .divide(student.getGroupAvgScore(), 3, RoundingMode.HALF_UP);
                    // 可以在这里保存调节系数到答辩小组
                }

                studentRepository.save(student);
            }
        }
    }

    /**
     * 转换为响应DTO
     */
    private FinalScoreResponse toResponse(Student student) {
        FinalScoreResponse response = new FinalScoreResponse();
        response.setStudentId(student.getId());
        response.setStudentNo(student.getStudentNo());
        response.setStudentName(student.getName());
        response.setThesisTitle(student.getThesisTitle());
        response.setThesisType(student.getThesisType() != null ? student.getThesisType().name() : null);
        response.setGroupId(student.getGroupId());
        response.setGroupAvgScore(student.getGroupAvgScore());
        response.setFinalScore(student.getFinalScore());

        // 获取小组名称
        if (student.getGroup() != null) {
            response.setGroupName(student.getGroup().getName());
        }

        return response;
    }
}
