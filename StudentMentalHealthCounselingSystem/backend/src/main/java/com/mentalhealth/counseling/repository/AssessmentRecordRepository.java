package com.mentalhealth.counseling.repository;

import com.mentalhealth.counseling.entity.AssessmentRecord;
import com.mentalhealth.counseling.entity.AssessmentRecord.AssessmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * 测评记录数据访问接口
 */
public interface AssessmentRecordRepository extends JpaRepository<AssessmentRecord, Long> {
    /**
     * 查找用户的测评记录，按创建时间倒序
     */
    Page<AssessmentRecord> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);

    /**
     * 查找用户在指定问卷的最新测评记录
     */
    Optional<AssessmentRecord> findFirstByUserIdAndQuestionnaireIdOrderByCreateTimeDesc(Long userId, Long questionnaireId);

    /**
     * 查找用户未完成的测评记录
     */
    List<AssessmentRecord> findByUserIdAndStatus(Long userId, AssessmentStatus status);

    /**
     * 查找问卷的所有记录
     */
    List<AssessmentRecord> findByQuestionnaireIdAndStatus(Long questionnaireId, AssessmentStatus status);

    /**
     * 统计问卷的完成次数
     */
    @Query("SELECT COUNT(ar) FROM AssessmentRecord ar WHERE ar.questionnaire.id = :questionnaireId AND ar.status = 'COMPLETED'")
    Long countCompletedByQuestionnaireId(Long questionnaireId);

    /**
     * 统计完成问卷的不同用户数量
     */
    @Query("SELECT COUNT(DISTINCT ar.user.id) FROM AssessmentRecord ar WHERE ar.questionnaire.id = :questionnaireId AND ar.status = 'COMPLETED'")
    Long countDistinctUsersByQuestionnaireId(Long questionnaireId);

    /**
     * 计算问卷的平均分
     */
    @Query("SELECT COALESCE(AVG(ar.totalScore), 0) FROM AssessmentRecord ar WHERE ar.questionnaire.id = :questionnaireId AND ar.status = 'COMPLETED'")
    Double calculateAverageScore(Long questionnaireId);
} 