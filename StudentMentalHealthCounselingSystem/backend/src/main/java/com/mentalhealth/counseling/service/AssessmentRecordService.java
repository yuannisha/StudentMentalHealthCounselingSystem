package com.mentalhealth.counseling.service;

import com.mentalhealth.counseling.dto.assessment.AssessmentResultResponse;
import com.mentalhealth.counseling.dto.assessment.AssessmentSubmitRequest;
import com.mentalhealth.counseling.dto.assessment.QuestionnaireStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 测评记录服务接口
 */
public interface AssessmentRecordService {
    /**
     * 提交测评答案
     */
    AssessmentResultResponse submitAnswers(Long userId, AssessmentSubmitRequest request);

    /**
     * 获取测评结果
     */
    AssessmentResultResponse getAssessmentResult(Long userId, Long recordId);

    /**
     * 获取用户的测评历史
     */
    Page<AssessmentResultResponse> getAssessmentHistory(Long userId, Pageable pageable);

    /**
     * 获取问卷的统计数据
     */
    QuestionnaireStatistics getQuestionnaireStatistics(Long questionnaireId);
} 