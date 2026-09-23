package com.defense3.demo.repository;

import com.defense3.demo.entity.User;
import com.defense3.demo.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByTeacherNo(String teacherNo);

    boolean existsByUsername(String username);

    boolean existsByTeacherNo(String teacherNo);

    List<User> findByDepartmentIdAndRole(Long departmentId, UserRole role);

    Page<User> findByDepartmentId(Long departmentId, Pageable pageable);

    Page<User> findByRole(UserRole role, Pageable pageable);

    List<User> findByRole(UserRole role);

    Page<User> findByDepartmentIdAndRole(Long departmentId, UserRole role, Pageable pageable);

    List<User> findByDepartmentId(Long departmentId);

    // 查询指定角色列表的用户
    List<User> findByRoleIn(List<UserRole> roles);

    List<User> findByDepartmentIdAndRoleIn(Long departmentId, List<UserRole> roles);

    long countByDepartmentIdAndRole(Long departmentId, UserRole role);

    // 查询纯管理员（没有工号的DEPT_ADMIN）
    List<User> findByRoleAndTeacherNoIsNull(UserRole role);

    // 查询有工号的用户（教师，包括升级为管理员的）
    List<User> findByTeacherNoIsNotNull();

    List<User> findByDepartmentIdAndTeacherNoIsNotNull(Long departmentId);
}
