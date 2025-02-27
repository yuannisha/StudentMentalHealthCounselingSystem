package com.mentalhealth.counseling.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户测评记录实体类
 */
@Data
@Entity
@Table(name = "assessment_record")
public class AssessmentRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionnaire_id", nullable = false)
    private AssessmentQuestionnaire questionnaire;

    private Integer totalScore = 0;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AssessmentStatus status;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "complete_time")
    private LocalDateTime completeTime;

    @OneToMany(mappedBy = "assessmentRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnswerRecord> answers = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    /**
     * 测评状态枚举
     */
    public enum AssessmentStatus {
        IN_PROGRESS,  // 进行中
        COMPLETED     // 已完成
    }
} 