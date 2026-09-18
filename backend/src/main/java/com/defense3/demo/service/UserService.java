package com.defense3.demo.service;

import com.defense3.demo.dto.UserRequest;
import com.defense3.demo.dto.UserResponse;
import com.defense3.demo.entity.Department;
import com.defense3.demo.entity.User;
import com.defense3.demo.entity.UserRole;
import com.defense3.demo.exception.BusinessException;
import com.defense3.demo.repository.DepartmentRepository;
import com.defense3.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 用户服务
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 分页获取用户列表
     */
    public Page<UserResponse> findAll(Long departmentId, UserRole role, String keyword, Pageable pageable) {
        Page<User> users;

        if (departmentId != null && role != null) {
            users = userRepository.findByDepartmentIdAndRole(departmentId, role, pageable);
        } else if (departmentId != null) {
            users = userRepository.findByDepartmentId(departmentId, pageable);
        } else if (role != null) {
            users = userRepository.findByRole(role, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(user -> {
            String deptName = null;
            if (user.getDepartmentId() != null) {
                deptName = departmentRepository.findById(user.getDepartmentId())
                        .map(Department::getName)
                        .orElse(null);
            }
            return UserResponse.fromEntity(user, deptName);
        });
    }

    /**
     * 获取教师列表（指定院系，或全部）
     * 只查询有工号的用户（原本是教师的，包括升级为管理员的）
     */
    public List<UserResponse> findTeachersByDepartment(Long departmentId) {
        List<User> teachers;
        if (departmentId != null) {
            teachers = userRepository.findByDepartmentIdAndTeacherNoIsNotNull(departmentId);
        } else {
            teachers = userRepository.findByTeacherNoIsNotNull();
        }
        return teachers.stream()
                .map(user -> {
                    String deptName = null;
                    if (user.getDepartmentId() != null) {
                        deptName = departmentRepository.findById(user.getDepartmentId())
                                .map(Department::getName).orElse(null);
                    }
                    return UserResponse.fromEntity(user, deptName);
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取所有管理员列表（所有DEPT_ADMIN角色）
     */
    public List<UserResponse> findAllAdmins() {
        List<User> admins = userRepository.findByRole(UserRole.DEPT_ADMIN);
        return admins.stream()
                .map(user -> {
                    String deptName = null;
                    if (user.getDepartmentId() != null) {
                        deptName = departmentRepository.findById(user.getDepartmentId())
                                .map(Department::getName).orElse(null);
                    }
                    return UserResponse.fromEntity(user, deptName);
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取用户
     */
    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        String deptName = null;
        if (user.getDepartmentId() != null) {
            deptName = departmentRepository.findById(user.getDepartmentId())
                    .map(Department::getName)
                    .orElse(null);
        }
        return UserResponse.fromEntity(user, deptName);
    }

    /**
     * 创建用户
     */
    @Transactional
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        if (StringUtils.hasText(request.getTeacherNo()) &&
                userRepository.existsByTeacherNo(request.getTeacherNo())) {
            throw new BusinessException("教师工号已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(
                StringUtils.hasText(request.getPassword()) ? request.getPassword() : "123456"));
        user.setName(request.getName());
        user.setTeacherNo(request.getTeacherNo());
        user.setDepartmentId(request.getDepartmentId());
        user.setRole(request.getRole() != null ? request.getRole() : UserRole.TEACHER);
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setEnabled(true);

        user = userRepository.save(user);

        String deptName = null;
        if (user.getDepartmentId() != null) {
            deptName = departmentRepository.findById(user.getDepartmentId())
                    .map(Department::getName)
                    .orElse(null);
        }
        return UserResponse.fromEntity(user, deptName);
    }

    /**
     * 更新用户
     */
    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 检查用户名是否重复
        if (!user.getUsername().equals(request.getUsername()) &&
                userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 检查工号是否重复
        if (StringUtils.hasText(request.getTeacherNo()) &&
                !request.getTeacherNo().equals(user.getTeacherNo()) &&
                userRepository.existsByTeacherNo(request.getTeacherNo())) {
            throw new BusinessException("教师工号已存在");
        }

        user.setUsername(request.getUsername());
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setName(request.getName());
        user.setTeacherNo(request.getTeacherNo());
        user.setDepartmentId(request.getDepartmentId());
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());

        user = userRepository.save(user);

        String deptName = null;
        if (user.getDepartmentId() != null) {
            deptName = departmentRepository.findById(user.getDepartmentId())
                    .map(Department::getName)
                    .orElse(null);
        }
        return UserResponse.fromEntity(user, deptName);
    }

    /**
     * 删除用户
     */
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new BusinessException("用户不存在");
        }
        userRepository.deleteById(id);
    }

    /**
     * 启用/禁用用户
     */
    @Transactional
    public void toggleEnabled(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setEnabled(!user.getEnabled());
        userRepository.save(user);
    }

    /**
     * 重置密码
     */
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * 上传教师签名
     */
    @Transactional
    public String uploadSignature(Long id, MultipartFile file) throws IOException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".png";
        String filename = "teacher_" + id + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

        Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads", "signatures");
        Files.createDirectories(uploadPath);
        Path filePath = uploadPath.resolve(filename);
        Files.write(filePath, file.getBytes());

        String signaturePath = "/uploads/signatures/" + filename;
        user.setSignature(signaturePath);
        userRepository.save(user);

        return signaturePath;
    }

    /**
     * 移除管理员身份
     * 如果是教师升级的，保留账号并降级为TEACHER
     * 如果是纯管理员，直接删除账号
     */
    @Transactional
    public void removeAdmin(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 如果是教师（有工号），则降级
        if (StringUtils.hasText(user.getTeacherNo())) {
            user.setRole(UserRole.TEACHER);
            // 清除可能管理的院系关联
            departmentRepository.findByAdminId(id).ifPresent(dept -> {
                dept.setAdminId(null);
                departmentRepository.save(dept);
            });
            userRepository.save(user);
        } else {
            // 纯管理员，直接删除
            // 先清除院系关联
            departmentRepository.findByAdminId(id).ifPresent(dept -> {
                dept.setAdminId(null);
                departmentRepository.save(dept);
            });
            userRepository.delete(user);
        }
    }
}
