package com.defense3.demo.repository;

import com.defense3.demo.entity.GroupScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupScoreRepository extends JpaRepository<GroupScore, Long> {

    Optional<GroupScore> findByStudentIdAndTeacherId(Long studentId, Long teacherId);

    Optional<GroupScore> findByStudentIdAndTeacherIdAndYearId(Long studentId, Long teacherId, Long yearId);

    List<GroupScore> findByStudentId(Long studentId);

    List<GroupScore> findByStudentIdAndYearId(Long studentId, Long yearId);

    List<GroupScore> findByTeacherIdAndYearId(Long teacherId, Long yearId);

    @Query("SELECT AVG(gs.totalScore) FROM GroupScore gs WHERE gs.studentId = :studentId AND gs.yearId = :yearId")
    Double getAverageScoreByStudentIdAndYearId(@Param("studentId") Long studentId, @Param("yearId") Long yearId);

    @Query("SELECT AVG(gs.item1Score) FROM GroupScore gs WHERE gs.studentId = :studentId AND gs.yearId = :yearId")
    Double getAverageItem1Score(@Param("studentId") Long studentId, @Param("yearId") Long yearId);

    @Query("SELECT AVG(gs.item2Score) FROM GroupScore gs WHERE gs.studentId = :studentId AND gs.yearId = :yearId")
    Double getAverageItem2Score(@Param("studentId") Long studentId, @Param("yearId") Long yearId);

    @Query("SELECT AVG(gs.item3Score) FROM GroupScore gs WHERE gs.studentId = :studentId AND gs.yearId = :yearId")
    Double getAverageItem3Score(@Param("studentId") Long studentId, @Param("yearId") Long yearId);

    @Query("SELECT AVG(gs.item4Score) FROM GroupScore gs WHERE gs.studentId = :studentId AND gs.yearId = :yearId")
    Double getAverageItem4Score(@Param("studentId") Long studentId, @Param("yearId") Long yearId);

    @Query("SELECT AVG(gs.item5Score) FROM GroupScore gs WHERE gs.studentId = :studentId AND gs.yearId = :yearId")
    Double getAverageItem5Score(@Param("studentId") Long studentId, @Param("yearId") Long yearId);

    @Query("SELECT AVG(gs.item6Score) FROM GroupScore gs WHERE gs.studentId = :studentId AND gs.yearId = :yearId")
    Double getAverageItem6Score(@Param("studentId") Long studentId, @Param("yearId") Long yearId);

    boolean existsByStudentIdAndTeacherIdAndYearId(Long studentId, Long teacherId, Long yearId);

    long countByStudentId(Long studentId);
}
