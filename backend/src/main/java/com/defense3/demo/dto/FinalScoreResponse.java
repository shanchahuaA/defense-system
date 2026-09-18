package com.defense3.demo.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 大组答辩评分响应DTO
 */
@Data
public class FinalScoreResponse {

    private Long studentId;
    private String studentNo;
    private String studentName;
    private String thesisTitle;
    private String thesisType;
    private Long groupId;
    private String groupName;
    private BigDecimal groupAvgScore;
    private BigDecimal finalScore;
    private Integer myScore; // 当前教师的评分
}
