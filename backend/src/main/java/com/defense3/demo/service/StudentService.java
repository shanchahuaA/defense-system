package com.defense3.demo.service;

import com.defense3.demo.dto.StudentRequest;
import com.defense3.demo.dto.StudentResponse;
import com.defense3.demo.entity.*;
import com.defense3.demo.exception.BusinessException;
import com.defense3.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 学生服务
 */
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final DefenseGroupRepository defenseGroupRepository;
    private final DefenseYearRepository defenseYearRepository;

    /**
     * 分页获取学生列表
     */
    public Page<StudentResponse> findAll(Long departmentId, Long groupId, Long yearId,
            ThesisType thesisType, Pageable pageable) {
        Page<Student> students;

        if (groupId != null) {
            students = studentRepository.findByGroupId(groupId, pageable);
        } else if (departmentId != null && yearId != null && thesisType != null) {
            students = studentRepository.findByDepartmentIdAndYearIdAndThesisType(
                    departmentId, yearId, thesisType, pageable);
        } else if (departmentId != null && yearId != null) {
            students = studentRepository.findByDepartmentIdAndYearId(departmentId, yearId, pageable);
        } else if (departmentId != null && thesisType != null) {
            // 按院系和论文类型筛选（不限年份）
            students = studentRepository.findByDepartmentIdAndThesisType(departmentId, thesisType, pageable);
        } else if (departmentId != null) {
            students = studentRepository.findByDepartmentId(departmentId, pageable);
        } else if (thesisType != null) {
            // 仅按论文类型筛选（不限院系）
            students = studentRepository.findByThesisType(thesisType, pageable);
        } else {
            students = studentRepository.findAll(pageable);
        }

        return students.map(this::convertToResponse);
    }

    /**
     * 获取指导教师的学生列表
     */
    public List<StudentResponse> findByAdvisor(Long advisorId) {
        List<Student> students = studentRepository.findByAdvisorId(advisorId);
        return students.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * 获取答辩小组的学生列表（按答辩顺序）
     */
    public List<StudentResponse> findByGroupOrderByDefenseOrder(Long groupId) {
        List<Student> students = studentRepository.findByGroupIdOrderByDefenseOrder(groupId);
        return students.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * 根据ID获取学生
     */
    public StudentResponse findById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("学生不存在"));
        return convertToResponse(student);
    }

    /**
     * 创建学生
     */
    @Transactional
    public StudentResponse create(StudentRequest request, Long yearId) {
        if (studentRepository.existsByStudentNo(request.getStudentNo())) {
            throw new BusinessException("学号已存在");
        }

        Student student = new Student();
        copyRequestToEntity(request, student);
        student.setYearId(yearId);

        student = studentRepository.save(student);
        return convertToResponse(student);
    }

    /**
     * 更新学生
     */
    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("学生不存在"));

        // 检查学号是否重复
        if (!student.getStudentNo().equals(request.getStudentNo()) &&
                studentRepository.existsByStudentNo(request.getStudentNo())) {
            throw new BusinessException("学号已存在");
        }

        copyRequestToEntity(request, student);
        student = studentRepository.save(student);
        return convertToResponse(student);
    }

    /**
     * 删除学生
     */
    @Transactional
    public void delete(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new BusinessException("学生不存在");
        }
        studentRepository.deleteById(id);
    }

    /**
     * 分配答辩小组
     */
    @Transactional
    public void assignGroup(Long studentId, Long groupId, Integer defenseOrder) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("学生不存在"));

        if (groupId != null && !defenseGroupRepository.existsById(groupId)) {
            throw new BusinessException("答辩小组不存在");
        }

        student.setGroupId(groupId);
        student.setDefenseOrder(defenseOrder);
        studentRepository.save(student);
    }

    /**
     * 分配指导教师
     */
    @Transactional
    public void assignAdvisor(Long studentId, Long advisorId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("学生不存在"));

        if (advisorId != null && !userRepository.existsById(advisorId)) {
            throw new BusinessException("指导教师不存在");
        }

        student.setAdvisorId(advisorId);
        studentRepository.save(student);
    }

    /**
     * 分配评阅人
     */
    @Transactional
    public void assignReviewer(Long studentId, Long reviewerId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("学生不存在"));

        if (reviewerId != null && !userRepository.existsById(reviewerId)) {
            throw new BusinessException("评阅人不存在");
        }

        student.setReviewerId(reviewerId);
        studentRepository.save(student);
    }

    private void copyRequestToEntity(StudentRequest request, Student student) {
        student.setStudentNo(request.getStudentNo());
        student.setName(request.getName());
        student.setClassName(request.getClassName());
        student.setDepartmentId(request.getDepartmentId());
        student.setMajor(request.getMajor());
        student.setThesisTitle(request.getThesisTitle());
        student.setThesisType(request.getThesisType());
        student.setAdvisorId(request.getAdvisorId());
        student.setReviewerId(request.getReviewerId());
        student.setGroupId(request.getGroupId());
        student.setDefenseOrder(request.getDefenseOrder());
        student.setPhone(request.getPhone());
        student.setEmail(request.getEmail());
    }

    private StudentResponse convertToResponse(Student student) {
        String deptName = null;
        String advisorName = null;
        String reviewerName = null;
        String groupName = null;

        if (student.getDepartmentId() != null) {
            deptName = departmentRepository.findById(student.getDepartmentId())
                    .map(Department::getName).orElse(null);
        }
        if (student.getAdvisorId() != null) {
            advisorName = userRepository.findById(student.getAdvisorId())
                    .map(User::getName).orElse(null);
        }
        if (student.getReviewerId() != null) {
            reviewerName = userRepository.findById(student.getReviewerId())
                    .map(User::getName).orElse(null);
        }
        if (student.getGroupId() != null) {
            groupName = defenseGroupRepository.findById(student.getGroupId())
                    .map(DefenseGroup::getName).orElse(null);
        }

        return StudentResponse.fromEntity(student, deptName, advisorName, reviewerName, groupName);
    }
}
