package com.defense3.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 院系请求DTO
 */
@Data
public class DepartmentRequest {

    @NotBlank(message = "院系名称不能为空")
    private String name;

    private String deanName;

    private String description;
}
