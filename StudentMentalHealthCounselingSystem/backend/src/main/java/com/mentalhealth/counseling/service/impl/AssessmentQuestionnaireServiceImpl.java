package com.mentalhealth.counseling.service.impl;

import com.mentalhealth.counseling.dto.assessment.QuestionnaireRequest;
import com.mentalhealth.counseling.dto.assessment.QuestionnaireResponse;
import com.mentalhealth.counseling.entity.AssessmentQuestion;
import com.mentalhealth.counseling.entity.AssessmentQuestionnaire;
import com.mentalhealth.counseling.entity.AssessmentQuestionnaire.QuestionnaireStatus;
import com.mentalhealth.counseling.entity.QuestionOption;
import com.mentalhealth.counseling.exception.ResourceNotFoundException;
import com.mentalhealth.counseling.repository.AssessmentQuestionnaireRepository;
import com.mentalhealth.counseling.service.AssessmentQuestionnaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 问卷管理服务实现类
 */
@Service
@RequiredArgsConstructor
public class AssessmentQuestionnaireServiceImpl implements AssessmentQuestionnaireService {

    private final AssessmentQuestionnaireRepository questionnaireRepository;
    private static final Logger log = LoggerFactory.getLogger(AssessmentQuestionnaireServiceImpl.class);

    @Override
    @Transactional
    public QuestionnaireResponse createQuestionnaire(QuestionnaireRequest request) {
        AssessmentQuestionnaire questionnaire = new AssessmentQuestionnaire();
        questionnaire.setTitle(request.getTitle());
        questionnaire.setDescription(request.getDescription());
        questionnaire.setPassingScore(request.getPassingScore());
        questionnaire.setStatus(QuestionnaireStatus.DRAFT);
        questionnaire.setQuestions(new ArrayList<>());

        // 创建题目和选项
        request.getQuestions().forEach(questionRequest -> {
            AssessmentQuestion question = new AssessmentQuestion();
            question.setQuestionText(questionRequest.getQuestionText());
            question.setQuestionType(AssessmentQuestion.QuestionType.valueOf(questionRequest.getQuestionType()));
            question.setScore(questionRequest.getScore());
            question.setSortOrder(questionRequest.getSortOrder());
            question.setQuestionnaire(questionnaire);
            question.setOptions(new ArrayList<>());

            // 创建选项
            questionRequest.getOptions().forEach(optionRequest -> {
                QuestionOption option = new QuestionOption();
                option.setOptionText(optionRequest.getOptionText());
                option.setScore(optionRequest.getScore());
                option.setSortOrder(optionRequest.getSortOrder());
                option.setQuestion(question);
                question.getOptions().add(option);
            });

            questionnaire.getQuestions().add(question);
        });

        // 计算总分
        questionnaire.setTotalScore(
            questionnaire.getQuestions().stream()
                .mapToInt(AssessmentQuestion::getScore)
                .sum()
        );

        AssessmentQuestionnaire savedQuestionnaire = questionnaireRepository.save(questionnaire);
        return convertToQuestionnaireResponse(savedQuestionnaire);
    }

    @Override
    @Transactional
    public QuestionnaireResponse updateQuestionnaire(Long id, QuestionnaireRequest request) {
        AssessmentQuestionnaire questionnaire = questionnaireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("问卷不存在"));

        if (questionnaire.getStatus() != QuestionnaireStatus.DRAFT) {
            throw new IllegalStateException("只能修改草稿状态的问卷");
        }

        questionnaire.setTitle(request.getTitle());
        questionnaire.setDescription(request.getDescription());
        questionnaire.setPassingScore(request.getPassingScore());

        // 清除原有题目和选项
        questionnaire.getQuestions().clear();

        // 创建新的题目和选项
        request.getQuestions().forEach(questionRequest -> {
            AssessmentQuestion question = new AssessmentQuestion();
            question.setQuestionText(questionRequest.getQuestionText());
            question.setQuestionType(AssessmentQuestion.QuestionType.valueOf(questionRequest.getQuestionType()));
            question.setScore(questionRequest.getScore());
            question.setSortOrder(questionRequest.getSortOrder());
            question.setQuestionnaire(questionnaire);
            question.setOptions(new ArrayList<>());

            questionRequest.getOptions().forEach(optionRequest -> {
                QuestionOption option = new QuestionOption();
                option.setOptionText(optionRequest.getOptionText());
                option.setScore(optionRequest.getScore());
                option.setSortOrder(optionRequest.getSortOrder());
                option.setQuestion(question);
                question.getOptions().add(option);
            });

            questionnaire.getQuestions().add(question);
        });

        // 更新总分
        questionnaire.setTotalScore(
            questionnaire.getQuestions().stream()
                .mapToInt(AssessmentQuestion::getScore)
                .sum()
        );

        AssessmentQuestionnaire savedQuestionnaire = questionnaireRepository.save(questionnaire);
        return convertToQuestionnaireResponse(savedQuestionnaire);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionnaireResponse getQuestionnaire(Long id) {
        AssessmentQuestionnaire questionnaire = questionnaireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("问卷不存在"));
        return convertToQuestionnaireResponse(questionnaire);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuestionnaireResponse> getQuestionnaires(List<QuestionnaireStatus> statuses, String keyword, Pageable pageable) {
        // 添加日志
        log.info("查询问卷列表，状态：{}，关键词：{}，分页：{}", statuses, keyword, pageable);
        
        Page<AssessmentQuestionnaire> questionnaires;
        
        // 检查状态列表是否为空
        boolean hasValidStatuses = statuses != null && !statuses.isEmpty();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            if (hasValidStatuses) {
                questionnaires = questionnaireRepository.searchQuestionnaires(statuses, keyword.trim(), pageable);
            } else {
                questionnaires = questionnaireRepository.searchAllQuestionnaires(keyword.trim(), pageable);
            }
        } else {
            if (hasValidStatuses) {
                questionnaires = questionnaireRepository.findByStatusIn(statuses, pageable);
            } else {
                questionnaires = questionnaireRepository.findAllQuestionnaires(pageable);
            }
        }
        
        // 查询结果日志
        log.info("查询结果：共 {} 条记录", questionnaires.getTotalElements());
        
        return questionnaires.map(this::convertToQuestionnaireResponse);
    }

    @Override
    @Transactional
    public void publishQuestionnaire(Long id) {
        AssessmentQuestionnaire questionnaire = questionnaireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("问卷不存在"));

        if (questionnaire.getStatus() != QuestionnaireStatus.DRAFT) {
            throw new IllegalStateException("只能发布草稿状态的问卷");
        }

        if (questionnaire.getQuestions().isEmpty()) {
            throw new IllegalStateException("问卷必须包含至少一个题目");
        }

        questionnaire.setStatus(QuestionnaireStatus.PUBLISHED);
        questionnaireRepository.save(questionnaire);
    }

    @Override
    @Transactional
    public void archiveQuestionnaire(Long id) {
        AssessmentQuestionnaire questionnaire = questionnaireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("问卷不存在"));

        if (questionnaire.getStatus() != QuestionnaireStatus.PUBLISHED) {
            throw new IllegalStateException("只能归档已发布的问卷");
        }

        questionnaire.setStatus(QuestionnaireStatus.ARCHIVED);
        questionnaireRepository.save(questionnaire);
    }

    @Override
    @Transactional
    public void deleteQuestionnaire(Long id) {
        AssessmentQuestionnaire questionnaire = questionnaireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("问卷不存在"));

        if (questionnaire.getStatus() != QuestionnaireStatus.DRAFT) {
            throw new IllegalStateException("只能删除草稿状态的问卷");
        }

        questionnaireRepository.delete(questionnaire);
    }

    /**
     * 将问卷实体转换为响应DTO
     */
    private QuestionnaireResponse convertToQuestionnaireResponse(AssessmentQuestionnaire questionnaire) {
        QuestionnaireResponse response = new QuestionnaireResponse();
        response.setId(questionnaire.getId());
        response.setTitle(questionnaire.getTitle());
        response.setDescription(questionnaire.getDescription());
        response.setTotalScore(questionnaire.getTotalScore());
        response.setPassingScore(questionnaire.getPassingScore());
        response.setStatus(questionnaire.getStatus());
        response.setCreateTime(questionnaire.getCreateTime());
        response.setUpdateTime(questionnaire.getUpdateTime());

        // 转换题目
        response.setQuestions(questionnaire.getQuestions().stream()
                .map(this::convertToQuestionResponse)
                .collect(Collectors.toList()));

        return response;
    }

    /**
     * 将题目实体转换为响应DTO
     */
    private QuestionnaireResponse.QuestionResponse convertToQuestionResponse(AssessmentQuestion question) {
        QuestionnaireResponse.QuestionResponse response = new QuestionnaireResponse.QuestionResponse();
        response.setId(question.getId());
        response.setQuestionText(question.getQuestionText());
        response.setQuestionType(question.getQuestionType().name());
        response.setScore(question.getScore());
        response.setSortOrder(question.getSortOrder());

        // 转换选项
        response.setOptions(question.getOptions().stream()
                .map(this::convertToOptionResponse)
                .collect(Collectors.toList()));

        return response;
    }

    /**
     * 将选项实体转换为响应DTO
     */
    private QuestionnaireResponse.OptionResponse convertToOptionResponse(QuestionOption option) {
        QuestionnaireResponse.OptionResponse response = new QuestionnaireResponse.OptionResponse();
        response.setId(option.getId());
        response.setOptionText(option.getOptionText());
        response.setScore(option.getScore());
        response.setSortOrder(option.getSortOrder());
        return response;
    }
} 