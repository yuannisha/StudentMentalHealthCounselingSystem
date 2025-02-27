package com.mentalhealth.counseling.repository;

import com.mentalhealth.counseling.entity.AssessmentQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 测评题目数据访问接口
 */
public interface AssessmentQuestionRepository extends JpaRepository<AssessmentQuestion, Long> {
    /**
     * 根据问卷ID查找题目列表，按排序字段升序
     */
    List<AssessmentQuestion> findByQuestionnaireIdOrderBySortOrderAsc(Long questionnaireId);

    /**
     * 获取问卷的总分
     */
    @Query("SELECT COALESCE(SUM(q.score), 0) FROM AssessmentQuestion q WHERE q.questionnaire.id = :questionnaireId")
    Integer calculateQuestionnaireScore(Long questionnaireId);
} 