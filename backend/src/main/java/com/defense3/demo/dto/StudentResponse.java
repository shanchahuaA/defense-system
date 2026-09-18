package com.defense3.demo.dto;

import com.defense3.demo.entity.Student;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生响应DTO
 */
@Data
public class StudentResponse {

    private Long id;
    private String studentNo;
    private String name;
    private String className;
    private Long departmentId;
    private String departmentName;
    private String major;
    private String thesisTitle;
    private String thesisType;
    private Long advisorId;
    private String advisorName;
    private Long reviewerId;
    private String reviewerName;
    private Long groupId;
    private String groupName;
    private Integer defenseOrder;
    private BigDecimal groupAvgScore;
    private BigDecimal finalGroupScore;
    private BigDecimal finalScore;
    private String aiComment;
    private String phone;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static StudentResponse fromEntity(Student student, String deptName,
            String advisorName, String reviewerName,
            String groupName) {
        StudentResponse response = new StudentResponse();
        response.setId(student.getId());
        response.setStudentNo(student.getStudentNo());
        response.setName(student.getName());
        response.setClassName(student.getClassName());
        response.setDepartmentId(student.getDepartmentId());
        response.setDepartmentName(deptName);
        response.setMajor(student.getMajor());
        response.setThesisTitle(student.getThesisTitle());
        response.setThesisType(student.getThesisType() != null ? student.getThesisType().name() : null);
        response.setAdvisorId(student.getAdvisorId());
        response.setAdvisorName(advisorName);
        response.setReviewerId(student.getReviewerId());
        response.setReviewerName(reviewerName);
        response.setGroupId(student.getGroupId());
        response.setGroupName(groupName);
        response.setDefenseOrder(student.getDefenseOrder());
        response.setGroupAvgScore(student.getGroupAvgScore());
        response.setFinalGroupScore(student.getFinalGroupScore());
        response.setFinalScore(student.getFinalScore());
        response.setAiComment(student.getAiComment());
        response.setPhone(student.getPhone());
        response.setEmail(student.getEmail());
        response.setCreatedAt(student.getCreatedAt());
        response.setUpdatedAt(student.getUpdatedAt());
        return response;
    }
}
