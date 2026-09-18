package com.defense3.demo.repository;

import com.defense3.demo.entity.FinalScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinalScoreRepository extends JpaRepository<FinalScore, Long> {

    Optional<FinalScore> findByStudentIdAndTeacherIdAndYearId(Long studentId, Long teacherId, Long yearId);

    List<FinalScore> findByStudentIdAndYearId(Long studentId, Long yearId);

    List<FinalScore> findByTeacherIdAndYearId(Long teacherId, Long yearId);

    @Query("SELECT AVG(fs.totalScore) FROM FinalScore fs WHERE fs.studentId = :studentId AND fs.yearId = :yearId")
    Double getAverageScoreByStudentIdAndYearId(@Param("studentId") Long studentId, @Param("yearId") Long yearId);

    boolean existsByStudentIdAndTeacherIdAndYearId(Long studentId, Long teacherId, Long yearId);
}
