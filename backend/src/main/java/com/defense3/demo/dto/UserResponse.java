package com.defense3.demo.dto;

import com.defense3.demo.entity.Department;
import com.defense3.demo.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户响应DTO
 */
@Data
public class UserResponse {

    private Long id;
    private String username;
    private String name;
    private String teacherNo;
    private Long departmentId;
    private String departmentName;
    private String role;
    private String phone;
    private String email;
    private String signature;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse fromEntity(User user, String departmentName) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setName(user.getName());
        response.setTeacherNo(user.getTeacherNo());
        response.setDepartmentId(user.getDepartmentId());
        response.setDepartmentName(departmentName);
        response.setRole(user.getRole().name());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setSignature(user.getSignature());
        response.setEnabled(user.getEnabled());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
