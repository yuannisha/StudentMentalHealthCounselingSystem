package com.mentalhealth.counseling.dto.assessment;

import lombok.Data;

/**
 * 问卷统计数据DTO
 */
@Data
public class QuestionnaireStatistics {
    private Long questionnaireId;
    private String title;
    private Long completedCount; // 总完成次数
    private Long distinctUsersCount; // 不同用户完成人数
    private Double averageScore;
    private Long passedCount;
    private Double passRate;
    private Long failedCount; // 未通过人数
    private Integer highestScore; // 最高分
    private Integer lowestScore; // 最低分
} 