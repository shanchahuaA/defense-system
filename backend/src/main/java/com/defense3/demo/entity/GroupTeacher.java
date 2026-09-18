package com.defense3.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 小组-教师关联实体
 */
@Data
@Entity
@Table(name = "group_teacher")
public class GroupTeacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    private DefenseGroup group;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", insertable = false, updatable = false)
    private User teacher;

    @Column(name = "teacher_order")
    private Integer teacherOrder = 1;
}
