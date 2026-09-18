package com.defense3.demo.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 登录响应DTO
 */
@Data
@Builder
public class LoginResponse {

    private String token;
    private Long userId;
    private String username;
    private String name;
    private String role;
    private Long departmentId;
    private String departmentName;
    private Long currentYearId;
    private String currentYearName;
    private Boolean isGroupLeader;
    private String signature;
}
