package com.mentalhealth.counseling.repository;

import com.mentalhealth.counseling.entity.AnswerRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 答题记录数据访问接口
 */
public interface AnswerRecordRepository extends JpaRepository<AnswerRecord, Long> {
    /**
     * 根据测评记录ID查找答题记录列表
     */
    List<AnswerRecord> findByAssessmentRecordId(Long assessmentRecordId);

    /**
     * 根据测评记录ID和题目ID查找答题记录
     */
    AnswerRecord findByAssessmentRecordIdAndQuestionId(Long assessmentRecordId, Long questionId);

    /**
     * 根据测评记录ID列表查找所有答题记录
     */
    List<AnswerRecord> findByAssessmentRecordIdIn(List<Long> assessmentRecordIds);
} 