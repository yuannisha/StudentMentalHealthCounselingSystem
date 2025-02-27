package com.mentalhealth.counseling.dto.assessment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 提交测评答案请求DTO
 */
@Data
public class AssessmentSubmitRequest {
    @NotNull(message = "问卷ID不能为空")
    private Long questionnaireId;

    @NotEmpty(message = "答案不能为空")
    @Valid
    private List<AnswerRequest> answers;

    /**
     * 答案请求DTO
     */
    @Data
    public static class AnswerRequest {
        @NotNull(message = "题目ID不能为空")
        private Long questionId;

        @NotEmpty(message = "选项不能为空")
        private List<Long> selectedOptions;
    }
} 