package com.defense3.demo.dto;

import com.defense3.demo.entity.Department;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 院系响应DTO
 */
@Data
public class DepartmentResponse {

    private Long id;
    private String name;
    private String deanName;
    private String deanSignature;
    private String description;
    private Long adminId;
    private String adminName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DepartmentResponse fromEntity(Department dept, String adminName) {
        DepartmentResponse response = new DepartmentResponse();
        response.setId(dept.getId());
        response.setName(dept.getName());
        response.setDeanName(dept.getDeanName());
        response.setDeanSignature(dept.getDeanSignature());
        response.setDescription(dept.getDescription());
        response.setAdminId(dept.getAdminId());
        response.setAdminName(adminName);
        response.setCreatedAt(dept.getCreatedAt());
        response.setUpdatedAt(dept.getUpdatedAt());
        return response;
    }
}
