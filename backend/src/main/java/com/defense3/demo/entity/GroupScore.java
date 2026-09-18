package com.defense3.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 小组答辩评分实体
 */
@Data
@Entity
@Table(name = "group_score")
public class GroupScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private Student student;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", insertable = false, updatable = false)
    private User teacher;

    @Column(name = "year_id", nullable = false)
    private Long yearId;

    @Column(name = "item1_score")
    private Integer item1Score = 0;

    @Column(name = "item2_score")
    private Integer item2Score = 0;

    @Column(name = "item3_score")
    private Integer item3Score = 0;

    @Column(name = "item4_score")
    private Integer item4Score = 0;

    @Column(name = "item5_score")
    private Integer item5Score = 0;

    @Column(name = "item6_score")
    private Integer item6Score = 0;

    @Column(name = "total_score")
    private Integer totalScore = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
