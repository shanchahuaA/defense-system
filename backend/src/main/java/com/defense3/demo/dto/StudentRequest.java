package com.defense3.demo.dto;

import com.defense3.demo.entity.ThesisType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 学生请求DTO
 */
@Data
public class StudentRequest {

    @NotBlank(message = "学号不能为空")
    private String studentNo;

    @NotBlank(message = "姓名不能为空")
    private String name;

    private String className;

    private Long departmentId;

    private String major;

    private String thesisTitle;

    private ThesisType thesisType;

    private Long advisorId;

    private Long reviewerId;

    private Long groupId;

    private Integer defenseOrder;

    private String phone;

    private String email;
}
