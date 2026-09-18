package com.defense3.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学生实体
 */
@Data
@Entity
@Table(name = "student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_no", nullable = false, length = 20)
    private String studentNo;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "class_name", length = 50)
    private String className;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", insertable = false, updatable = false)
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(name = "thesis_type", nullable = false)
    private ThesisType thesisType = ThesisType.PAPER;

    @Column(length = 100)
    private String major;

    @Column(name = "thesis_title", length = 255)
    private String thesisTitle;

    @Column(length = 255)
    private String title;

    @Column(name = "abstract_text", columnDefinition = "TEXT")
    private String abstractText;

    @Column(name = "advisor_id")
    private Long advisorId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advisor_id", insertable = false, updatable = false)
    private User advisor;

    @Column(name = "reviewer_id")
    private Long reviewerId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", insertable = false, updatable = false)
    private User reviewer;

    @Column(name = "advisor_score")
    private Integer advisorScore;

    @Column(name = "reviewer_score")
    private Integer reviewerScore;

    @Column(name = "group_id")
    private Long groupId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    private DefenseGroup group;

    @Column(name = "year_id", nullable = false)
    private Long yearId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "year_id", insertable = false, updatable = false)
    private DefenseYear year;

    @Column(name = "defense_date")
    private LocalDate defenseDate;

    @Column(name = "defense_location", length = 100)
    private String defenseLocation;

    @Column(name = "ai_comment", columnDefinition = "TEXT")
    private String aiComment;

    @Column(name = "defense_order")
    private Integer defenseOrder;

    @Column(name = "group_avg_score", precision = 5, scale = 2)
    private java.math.BigDecimal groupAvgScore;

    @Column(name = "final_group_score", precision = 5, scale = 2)
    private java.math.BigDecimal finalGroupScore;

    @Column(name = "final_score", precision = 5, scale = 2)
    private java.math.BigDecimal finalScore;

    @Column(name = "final_comment", columnDefinition = "TEXT")
    private String finalComment;

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
