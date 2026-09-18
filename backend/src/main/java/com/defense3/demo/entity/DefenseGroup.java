package com.defense3.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 答辩小组实体
 */
@Data
@Entity
@Table(name = "defense_group")
public class DefenseGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", insertable = false, updatable = false)
    private Department department;

    @Column(name = "leader_id")
    private Long leaderId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", insertable = false, updatable = false)
    private User leader;

    @Column(name = "year_id", nullable = false)
    private Long yearId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "year_id", insertable = false, updatable = false)
    private DefenseYear year;

    @Column(name = "adjustment_factor", precision = 5, scale = 3)
    private BigDecimal adjustmentFactor = new BigDecimal("1.000");

    @Enumerated(EnumType.STRING)
    @Column(name = "thesis_type")
    private ThesisType thesisType;

    @Column(length = 100)
    private String location;

    @Column(name = "defense_time", length = 50)
    private String defenseTime;

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
