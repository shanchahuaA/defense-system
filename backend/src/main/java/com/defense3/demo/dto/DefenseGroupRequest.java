package com.defense3.demo.dto;

import com.defense3.demo.entity.ThesisType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 答辩小组请求DTO
 */
@Data
public class DefenseGroupRequest {

    @NotBlank(message = "小组名称不能为空")
    private String name;

    private Long departmentId;

    private Long leaderId;

    private ThesisType thesisType;

    private String location;

    private String defenseTime;

    private List<Long> teacherIds;
}
