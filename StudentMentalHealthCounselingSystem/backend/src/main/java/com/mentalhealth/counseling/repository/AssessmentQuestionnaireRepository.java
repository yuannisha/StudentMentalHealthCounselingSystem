package com.mentalhealth.counseling.repository;

import com.mentalhealth.counseling.entity.AssessmentQuestionnaire;
import com.mentalhealth.counseling.entity.AssessmentQuestionnaire.QuestionnaireStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 心理测评问卷数据访问接口
 */
public interface AssessmentQuestionnaireRepository extends JpaRepository<AssessmentQuestionnaire, Long> {
    /**
     * 根据状态查找问卷
     */
    List<AssessmentQuestionnaire> findByStatus(QuestionnaireStatus status);

    /**
     * 分页查询问卷
     */
    Page<AssessmentQuestionnaire> findByStatusIn(List<QuestionnaireStatus> statuses, Pageable pageable);
    
    /**
     * 查询所有问卷（不考虑状态）
     */
    @Query("SELECT q FROM AssessmentQuestionnaire q")
    Page<AssessmentQuestionnaire> findAllQuestionnaires(Pageable pageable);

    /**
     * 根据标题模糊搜索问卷
     */
    @Query("SELECT q FROM AssessmentQuestionnaire q WHERE q.status IN :statuses AND " +
           "(LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(q.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<AssessmentQuestionnaire> searchQuestionnaires(List<QuestionnaireStatus> statuses, String keyword, Pageable pageable);
    
    /**
     * 根据标题模糊搜索所有问卷（不考虑状态）
     */
    @Query("SELECT q FROM AssessmentQuestionnaire q WHERE " +
           "LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(q.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<AssessmentQuestionnaire> searchAllQuestionnaires(String keyword, Pageable pageable);
} 