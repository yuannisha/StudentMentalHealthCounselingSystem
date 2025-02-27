package com.mentalhealth.counseling.dto.assessment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 问卷请求DTO
 */
@Data
public class QuestionnaireRequest {
    @NotBlank(message = "问卷标题不能为空")
    private String title;

    private String description;

    private Integer passingScore;

    @NotEmpty(message = "问卷必须包含至少一个题目")
    @Valid
    private List<QuestionRequest> questions;

    /**
     * 题目请求DTO
     */
    @Data
    public static class QuestionRequest {
        @NotBlank(message = "题目内容不能为空")
        private String questionText;

        @NotNull(message = "题目类型不能为空")
        private String questionType; // SINGLE_CHOICE, MULTIPLE_CHOICE

        @NotNull(message = "题目分数不能为空")
        private Integer score;

        private Integer sortOrder;

        @NotEmpty(message = "题目必须包含至少一个选项")
        @Valid
        private List<OptionRequest> options;
    }

    /**
     * 选项请求DTO
     */
    @Data
    public static class OptionRequest {
        @NotBlank(message = "选项内容不能为空")
        private String optionText;

        @NotNull(message = "选项分数不能为空")
        private Integer score;

        private Integer sortOrder;
    }
} 