package com.mentalhealth.counseling.repository;

import com.mentalhealth.counseling.entity.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 题目选项数据访问接口
 */
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {
    /**
     * 根据题目ID查找选项列表，按排序字段升序
     */
    List<QuestionOption> findByQuestionIdOrderBySortOrderAsc(Long questionId);

    /**
     * 根据题目ID列表查找所有选项
     */
    List<QuestionOption> findByQuestionIdIn(List<Long> questionIds);
} 