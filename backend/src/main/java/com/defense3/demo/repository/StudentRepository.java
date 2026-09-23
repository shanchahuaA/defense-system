package com.defense3.demo.repository;

import com.defense3.demo.entity.Student;
import com.defense3.demo.entity.ThesisType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

        Optional<Student> findByStudentNoAndYearId(String studentNo, Long yearId);

        boolean existsByStudentNoAndYearId(String studentNo, Long yearId);

        Page<Student> findByDepartmentIdAndYearId(Long departmentId, Long yearId, Pageable pageable);

        List<Student> findByGroupId(Long groupId);

        Page<Student> findByGroupId(Long groupId, Pageable pageable);

        List<Student> findByAdvisorId(Long advisorId);

        Page<Student> findByAdvisorIdAndYearId(Long advisorId, Long yearId, Pageable pageable);

        List<Student> findByReviewerId(Long reviewerId);

        Page<Student> findByYearId(Long yearId, Pageable pageable);

        List<Student> findByYearId(Long yearId);

        List<Student> findByYearIdAndThesisType(Long yearId, ThesisType thesisType);

        @Query("SELECT s FROM Student s WHERE s.groupId = :groupId ORDER BY " +
                        "(SELECT COALESCE(AVG(gs.totalScore), 0) FROM GroupScore gs WHERE gs.studentId = s.id) DESC")
        List<Student> findByGroupIdOrderByAvgScoreDesc(@Param("groupId") Long groupId);

        long countByDepartmentIdAndYearId(Long departmentId, Long yearId);

        long countByDepartmentId(Long departmentId);

        long countByGroupId(Long groupId);

        // 添加缺失的方法
        boolean existsByStudentNo(String studentNo);

        Page<Student> findByDepartmentId(Long departmentId, Pageable pageable);

        Page<Student> findByDepartmentIdAndYearIdAndThesisType(Long departmentId, Long yearId,
                        ThesisType thesisType, Pageable pageable);

        List<Student> findByGroupIdOrderByDefenseOrder(Long groupId);

        // 添加按院系和论文类型筛选
        Page<Student> findByDepartmentIdAndThesisType(Long departmentId, ThesisType thesisType, Pageable pageable);

        // 添加按论文类型筛选（不限院系）
        Page<Student> findByThesisType(ThesisType thesisType, Pageable pageable);

        // 根据多个小组ID查询学生
        List<Student> findByGroupIdIn(List<Long> groupIds);
}
