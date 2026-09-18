package com.defense3.demo.dto;

import com.defense3.demo.entity.DefenseGroup;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 答辩小组响应DTO
 */
@Data
public class DefenseGroupResponse {

    private Long id;
    private String name;
    private Long departmentId;
    private String departmentName;
    private Long yearId;
    private String yearName;
    private Long leaderId;
    private String leaderName;
    private String thesisType;
    private String location;
    private String defenseTime;
    private BigDecimal adjustmentFactor;
    private Integer studentCount;
    private List<TeacherInfo> teachers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class TeacherInfo {
        private Long id;
        private String name;
        private Integer teacherOrder;
    }

    public static DefenseGroupResponse fromEntity(DefenseGroup group, String deptName,
            String yearName, String leaderName,
            List<TeacherInfo> teachers, Integer studentCount) {
        DefenseGroupResponse response = new DefenseGroupResponse();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setDepartmentId(group.getDepartmentId());
        response.setDepartmentName(deptName);
        response.setYearId(group.getYearId());
        response.setYearName(yearName);
        response.setLeaderId(group.getLeaderId());
        response.setLeaderName(leaderName);
        response.setThesisType(group.getThesisType() != null ? group.getThesisType().name() : null);
        response.setLocation(group.getLocation());
        response.setDefenseTime(group.getDefenseTime());
        response.setAdjustmentFactor(group.getAdjustmentFactor());
        response.setStudentCount(studentCount);
        response.setTeachers(teachers);
        response.setCreatedAt(group.getCreatedAt());
        response.setUpdatedAt(group.getUpdatedAt());
        return response;
    }
}
