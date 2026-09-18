package com.defense3.demo.service;

import com.defense3.demo.dto.DepartmentRequest;
import com.defense3.demo.dto.DepartmentResponse;
import com.defense3.demo.entity.Department;
import com.defense3.demo.entity.User;
import com.defense3.demo.entity.UserRole;
import com.defense3.demo.exception.BusinessException;
import com.defense3.demo.repository.DepartmentRepository;
import com.defense3.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 院系服务
 */
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 获取所有院系
     */
    public List<DepartmentResponse> findAll() {
        return departmentRepository.findAll().stream()
                .map(dept -> {
                    String adminName = null;
                    if (dept.getAdminId() != null) {
                        adminName = userRepository.findById(dept.getAdminId())
                                .map(User::getName)
                                .orElse(null);
                    }
                    return DepartmentResponse.fromEntity(dept, adminName);
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取院系
     */
    public DepartmentResponse findById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("院系不存在"));
        String adminName = null;
        if (dept.getAdminId() != null) {
            adminName = userRepository.findById(dept.getAdminId())
                    .map(User::getName)
                    .orElse(null);
        }
        return DepartmentResponse.fromEntity(dept, adminName);
    }

    /**
     * 创建院系
     */
    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new BusinessException("院系名称已存在");
        }

        Department dept = new Department();
        dept.setName(request.getName());
        dept.setDeanName(request.getDeanName());
        dept.setDescription(request.getDescription());

        dept = departmentRepository.save(dept);
        return DepartmentResponse.fromEntity(dept, null);
    }

    /**
     * 更新院系
     */
    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("院系不存在"));

        // 检查名称是否重复
        if (!dept.getName().equals(request.getName()) &&
                departmentRepository.existsByName(request.getName())) {
            throw new BusinessException("院系名称已存在");
        }

        dept.setName(request.getName());
        dept.setDeanName(request.getDeanName());
        dept.setDescription(request.getDescription());

        dept = departmentRepository.save(dept);

        String adminName = null;
        if (dept.getAdminId() != null) {
            adminName = userRepository.findById(dept.getAdminId())
                    .map(User::getName)
                    .orElse(null);
        }
        return DepartmentResponse.fromEntity(dept, adminName);
    }

    /**
     * 删除院系
     */
    @Transactional
    public void delete(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new BusinessException("院系不存在");
        }
        // TODO: 检查是否有关联数据（教师、学生）
        departmentRepository.deleteById(id);
    }

    /**
     * 设置院系管理员
     */
    @Transactional
    public void setAdmin(Long deptId, Long userId) {
        Department dept = departmentRepository.findById(deptId)
                .orElseThrow(() -> new BusinessException("院系不存在"));

        // 如果之前有管理员，且该管理员是教师，是否需要将其降级？
        // 需求未明确说明，但通常替换管理员并不意味着前任失去教师身份，
        // 也不一定意味着前任失去管理员权限（可能是调岗），但在这里我们假设"系管理员"是该系唯一的。
        // 为了安全起见，我们暂不自动降级前任，只升级新任。
        // 如果用户希望前任降级，可以在管理员列表中操作删除。

        if (userId != null) {
            // 验证用户存在
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException("用户不存在"));

            // 升级为院系管理员
            if (user.getRole() == UserRole.TEACHER) {
                user.setRole(UserRole.DEPT_ADMIN);
                userRepository.save(user);
            }
        }

        dept.setAdminId(userId);
        departmentRepository.save(dept);
    }

    /**
     * 上传系主任签名
     */
    @Transactional
    public String uploadDeanSignature(Long id, MultipartFile file) throws IOException {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("院系不存在"));

        // 保存文件
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".png";
        String filename = "dean_" + id + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

        Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads", "signatures");
        Files.createDirectories(uploadPath);
        Path filePath = uploadPath.resolve(filename);
        Files.write(filePath, file.getBytes());

        String signaturePath = "/uploads/signatures/" + filename;
        dept.setDeanSignature(signaturePath);
        departmentRepository.save(dept);

        return signaturePath;
    }

    /**
     * 创建院系管理员（新建账号）
     */
    @Transactional
    public void createAdmin(Long deptId, String username, String name, String password) {
        Department dept = departmentRepository.findById(deptId)
                .orElseThrow(() -> new BusinessException("院系不存在"));

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("用户名已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setName(name);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(UserRole.DEPT_ADMIN);
        user.setDepartmentId(deptId);
        user.setEnabled(true);
        user = userRepository.save(user);

        // 设置为院系管理员
        dept.setAdminId(user.getId());
        departmentRepository.save(dept);
    }
}
