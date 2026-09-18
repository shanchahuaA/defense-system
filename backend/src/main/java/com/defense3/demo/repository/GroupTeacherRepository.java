package com.defense3.demo.repository;

import com.defense3.demo.entity.GroupTeacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupTeacherRepository extends JpaRepository<GroupTeacher, Long> {

    List<GroupTeacher> findByGroupIdOrderByTeacherOrder(Long groupId);

    List<GroupTeacher> findByTeacherId(Long teacherId);

    Optional<GroupTeacher> findByGroupIdAndTeacherId(Long groupId, Long teacherId);

    @org.springframework.data.jpa.repository.Modifying(flushAutomatically = true, clearAutomatically = true)
    @org.springframework.data.jpa.repository.Query("DELETE FROM GroupTeacher gt WHERE gt.groupId = :groupId")
    void deleteByGroupId(Long groupId);

    boolean existsByTeacherId(Long teacherId);

    boolean existsByTeacherIdAndGroupIdIn(Long teacherId, List<Long> groupIds);

    int countByGroupId(Long groupId);
}
