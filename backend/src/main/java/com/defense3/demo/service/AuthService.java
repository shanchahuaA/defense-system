package com.defense3.demo.service;

import com.defense3.demo.dto.ChangePasswordRequest;
import com.defense3.demo.dto.LoginRequest;
import com.defense3.demo.dto.LoginResponse;
import com.defense3.demo.entity.DefenseGroup;
import com.defense3.demo.entity.DefenseYear;
import com.defense3.demo.entity.Department;
import com.defense3.demo.entity.User;
import com.defense3.demo.exception.BusinessException;
import com.defense3.demo.repository.DefenseGroupRepository;
import com.defense3.demo.repository.DefenseYearRepository;
import com.defense3.demo.repository.DepartmentRepository;
import com.defense3.demo.repository.UserRepository;
import com.defense3.demo.security.CustomUserDetails;
import com.defense3.demo.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DefenseYearRepository defenseYearRepository;
    private final DefenseGroupRepository defenseGroupRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new BusinessException("用户名或密码错误");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (!user.getEnabled()) {
            throw new BusinessException("账号已被禁用");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole().name());

        // 获取院系信息
        String departmentName = null;
        if (user.getDepartmentId() != null) {
            departmentName = departmentRepository.findById(user.getDepartmentId())
                    .map(Department::getName)
                    .orElse(null);
        }

        // 获取当前年份
        List<DefenseYear> currentYears = defenseYearRepository.findByIsCurrent(true);
        DefenseYear currentYear = currentYears.isEmpty() ? null : currentYears.get(0);

        // 判断是否为组长
        Boolean isGroupLeader = false;
        if (currentYear != null) {
            List<DefenseGroup> leaderGroups = defenseGroupRepository.findByLeaderId(user.getId());
            isGroupLeader = leaderGroups.stream()
                    .anyMatch(g -> g.getYearId().equals(currentYear.getId()));
        }

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole().name())
                .departmentId(user.getDepartmentId())
                .departmentName(departmentName)
                .currentYearId(currentYear != null ? currentYear.getId() : null)
                .currentYearName(currentYear != null ? currentYear.getName() : null)
                .isGroupLeader(isGroupLeader)
                .signature(user.getSignature())
                .build();
    }

    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        CustomUserDetails userDetails = getCurrentUser();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * 获取当前用户信息
     */
    public LoginResponse getCurrentUserInfo() {
        CustomUserDetails userDetails = getCurrentUser();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 获取院系信息
        String departmentName = null;
        if (user.getDepartmentId() != null) {
            departmentName = departmentRepository.findById(user.getDepartmentId())
                    .map(Department::getName)
                    .orElse(null);
        }

        // 获取当前年份
        List<DefenseYear> currentYears2 = defenseYearRepository.findByIsCurrent(true);
        DefenseYear currentYear = currentYears2.isEmpty() ? null : currentYears2.get(0);

        // 判断是否为组长
        Boolean isGroupLeader = false;
        if (currentYear != null) {
            List<DefenseGroup> leaderGroups = defenseGroupRepository.findByLeaderId(user.getId());
            isGroupLeader = leaderGroups.stream()
                    .anyMatch(g -> g.getYearId().equals(currentYear.getId()));
        }

        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole().name())
                .departmentId(user.getDepartmentId())
                .departmentName(departmentName)
                .currentYearId(currentYear != null ? currentYear.getId() : null)
                .currentYearName(currentYear != null ? currentYear.getName() : null)
                .isGroupLeader(isGroupLeader)
                .signature(user.getSignature())
                .build();
    }

    /**
     * 获取当前登录用户
     */
    public CustomUserDetails getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return (CustomUserDetails) principal;
        }
        throw new BusinessException(401, "请先登录");
    }
}
