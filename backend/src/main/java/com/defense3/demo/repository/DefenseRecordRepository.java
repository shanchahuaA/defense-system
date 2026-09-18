package com.defense3.demo.repository;

import com.defense3.demo.entity.DefenseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DefenseRecordRepository extends JpaRepository<DefenseRecord, Long> {

    Optional<DefenseRecord> findByStudentId(Long studentId);

    boolean existsByStudentId(Long studentId);
}
