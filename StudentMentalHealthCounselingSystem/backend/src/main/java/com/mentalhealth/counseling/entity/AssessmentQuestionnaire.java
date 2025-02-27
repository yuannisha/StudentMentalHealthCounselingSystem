package com.mentalhealth.counseling.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 心理测评问卷实体类
 */
@Data
@Entity
@Table(name = "assessment_questionnaire")
public class AssessmentQuestionnaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer totalScore;

    private Integer passingScore;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private QuestionnaireStatus status = QuestionnaireStatus.DRAFT;

    @OneToMany(mappedBy = "questionnaire", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssessmentQuestion> questions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    /**
     * 问卷状态枚举
     */
    public enum QuestionnaireStatus {
        DRAFT,      // 草稿
        PUBLISHED,  // 已发布
        ARCHIVED    // 已归档
    }
} 