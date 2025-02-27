package com.mentalhealth.counseling.dto.assessment;

import com.mentalhealth.counseling.entity.AssessmentQuestionnaire.QuestionnaireStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 问卷响应DTO
 */
@Data
public class QuestionnaireResponse {
    private Long id;
    private String title;
    private String description;
    private Integer totalScore;
    private Integer passingScore;
    private QuestionnaireStatus status;
    private List<QuestionResponse> questions;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 题目响应DTO
     */
    @Data
    public static class QuestionResponse {
        private Long id;
        private String questionText;
        private String questionType;
        private Integer score;
        private Integer sortOrder;
        private List<OptionResponse> options;
    }

    /**
     * 选项响应DTO
     */
    @Data
    public static class OptionResponse {
        private Long id;
        private String optionText;
        private Integer score;
        private Integer sortOrder;
    }
} 