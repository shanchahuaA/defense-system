package com.defense3.demo.repository;

import com.defense3.demo.entity.DefenseGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DefenseGroupRepository extends JpaRepository<DefenseGroup, Long> {

    List<DefenseGroup> findByDepartmentIdAndYearId(Long departmentId, Long yearId);

    Page<DefenseGroup> findByDepartmentIdAndYearId(Long departmentId, Long yearId, Pageable pageable);

    List<DefenseGroup> findByYearId(Long yearId);

    List<DefenseGroup> findByLeaderId(Long leaderId);

    boolean existsByLeaderIdAndYearId(Long leaderId, Long yearId);

    Optional<DefenseGroup> findByNameAndDepartmentIdAndYearId(String name, Long departmentId, Long yearId);
}
