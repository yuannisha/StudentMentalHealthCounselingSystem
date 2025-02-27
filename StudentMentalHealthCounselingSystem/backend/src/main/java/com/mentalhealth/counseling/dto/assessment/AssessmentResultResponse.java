package com.mentalhealth.counseling.dto.assessment;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AssessmentResultResponse {
    private Long id;
    private Integer totalScore;
    private boolean passed;
    private QuestionnaireResponse questionnaire;
    private List<AnswerResultResponse> answers;
    private LocalDateTime createTime;

    @Data
    public static class AnswerResultResponse {
        private Long questionId;
        private List<Long> selectedOptions;
        private List<Long> correctOptions;
        private Integer score;
    }
} 