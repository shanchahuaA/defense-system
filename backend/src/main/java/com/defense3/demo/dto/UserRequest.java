package com.defense3.demo.dto;

import com.defense3.demo.entity.UserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户请求DTO
 */
@Data
public class UserRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    private String password;

    @NotBlank(message = "姓名不能为空")
    private String name;

    private String teacherNo;

    private Long departmentId;

    private UserRole role;

    private String phone;

    private String email;
}
