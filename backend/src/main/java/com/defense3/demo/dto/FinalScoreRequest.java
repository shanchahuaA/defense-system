package com.defense3.demo.dto;

import lombok.Data;

/**
 * 大组答辩评分请求DTO
 */
@Data
public class FinalScoreRequest {

    private Long studentId;
    private Long teacherId;
    private Long yearId;
    private Integer totalScore;
}
