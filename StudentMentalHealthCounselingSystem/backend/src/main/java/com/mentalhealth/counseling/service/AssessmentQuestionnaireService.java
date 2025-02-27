package com.mentalhealth.counseling.service;

import com.mentalhealth.counseling.dto.assessment.QuestionnaireRequest;
import com.mentalhealth.counseling.dto.assessment.QuestionnaireResponse;
import com.mentalhealth.counseling.entity.AssessmentQuestionnaire.QuestionnaireStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 问卷管理服务接口
 */
public interface AssessmentQuestionnaireService {
    /**
     * 创建问卷
     */
    QuestionnaireResponse createQuestionnaire(QuestionnaireRequest request);

    /**
     * 更新问卷
     */
    QuestionnaireResponse updateQuestionnaire(Long id, QuestionnaireRequest request);

    /**
     * 获取问卷详情
     */
    QuestionnaireResponse getQuestionnaire(Long id);

    /**
     * 分页查询问卷列表
     */
    Page<QuestionnaireResponse> getQuestionnaires(List<QuestionnaireStatus> statuses, String keyword, Pageable pageable);

    /**
     * 发布问卷
     */
    void publishQuestionnaire(Long id);

    /**
     * 归档问卷
     */
    void archiveQuestionnaire(Long id);

    /**
     * 删除问卷
     */
    void deleteQuestionnaire(Long id);
} 