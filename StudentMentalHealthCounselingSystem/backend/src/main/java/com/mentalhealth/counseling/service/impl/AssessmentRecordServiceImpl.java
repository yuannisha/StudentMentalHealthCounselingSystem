package com.mentalhealth.counseling.service.impl;

import com.mentalhealth.counseling.dto.assessment.AssessmentResultResponse;
import com.mentalhealth.counseling.dto.assessment.AssessmentSubmitRequest;
import com.mentalhealth.counseling.dto.assessment.QuestionnaireResponse;
import com.mentalhealth.counseling.dto.assessment.QuestionnaireStatistics;
import com.mentalhealth.counseling.entity.*;
import com.mentalhealth.counseling.exception.ResourceNotFoundException;
import com.mentalhealth.counseling.repository.*;
import com.mentalhealth.counseling.service.AssessmentRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 测评记录服务实现类
 */
@Service
@RequiredArgsConstructor
public class AssessmentRecordServiceImpl implements AssessmentRecordService {

    private final UserRepository userRepository;
    private final AssessmentQuestionnaireRepository questionnaireRepository;
    private final AssessmentRecordRepository recordRepository;
    @Override
    @Transactional
    public AssessmentResultResponse submitAnswers(Long userId, AssessmentSubmitRequest request) {
        // 获取用户和问卷
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        AssessmentQuestionnaire questionnaire = questionnaireRepository.findById(request.getQuestionnaireId())
                .orElseThrow(() -> new ResourceNotFoundException("问卷不存在"));

        // 创建测评记录
        AssessmentRecord record = new AssessmentRecord();
        record.setUser(user);
        record.setQuestionnaire(questionnaire);
        record.setStartTime(LocalDateTime.now());
        record.setCompleteTime(LocalDateTime.now());
        record.setStatus(AssessmentRecord.AssessmentStatus.COMPLETED);

        // 处理答案
        Map<Long, AssessmentQuestion> questionMap = questionnaire.getQuestions().stream()
                .collect(Collectors.toMap(AssessmentQuestion::getId, q -> q));
        Map<Long, QuestionOption> optionMap = questionnaire.getQuestions().stream()
                .flatMap(q -> q.getOptions().stream())
                .collect(Collectors.toMap(QuestionOption::getId, o -> o));

        List<AnswerRecord> answers = new ArrayList<>();
        int totalScore = 0;

        for (AssessmentSubmitRequest.AnswerRequest answerRequest : request.getAnswers()) {
            AssessmentQuestion question = questionMap.get(answerRequest.getQuestionId());
            if (question == null) {
                throw new ResourceNotFoundException("题目不存在");
            }

            // 计算得分
            int score = calculateScore(question, answerRequest.getSelectedOptions(), optionMap);
            totalScore += score;

            // 创建答题记录
            AnswerRecord answer = new AnswerRecord();
            answer.setAssessmentRecord(record);
            answer.setQuestion(question);
            answer.setSelectedOptionIds(String.join(",", answerRequest.getSelectedOptions().stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList())));
            answer.setScore(score);
            answers.add(answer);
        }

        record.setTotalScore(totalScore);
        record.setAnswers(answers);
        recordRepository.save(record);

        return convertToAssessmentResultResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public AssessmentResultResponse getAssessmentResult(Long userId, Long recordId) {
        AssessmentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("测评记录不存在"));

        // 验证记录所属用户
        if (!record.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("测评记录不存在");
        }

        return convertToAssessmentResultResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssessmentResultResponse> getAssessmentHistory(Long userId, Pageable pageable) {
        return recordRepository.findByUserIdOrderByCreateTimeDesc(userId, pageable)
                .map(this::convertToAssessmentResultResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionnaireStatistics getQuestionnaireStatistics(Long questionnaireId) {
        AssessmentQuestionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new ResourceNotFoundException("问卷不存在"));

        Long completedCount = recordRepository.countCompletedByQuestionnaireId(questionnaireId);
        Long distinctUsersCount = recordRepository.countDistinctUsersByQuestionnaireId(questionnaireId);
        Double averageScore = recordRepository.calculateAverageScore(questionnaireId);

        QuestionnaireStatistics statistics = new QuestionnaireStatistics();
        statistics.setQuestionnaireId(questionnaireId);
        statistics.setTitle(questionnaire.getTitle());
        statistics.setCompletedCount(completedCount);
        statistics.setDistinctUsersCount(distinctUsersCount);
        statistics.setAverageScore(averageScore != null ? averageScore : 0.0);

        // 计算通过率
        if (questionnaire.getPassingScore() != null && completedCount > 0) {
            Long passedCount = recordRepository.findByQuestionnaireIdAndStatus(questionnaireId, 
                    AssessmentRecord.AssessmentStatus.COMPLETED)
                    .stream()
                    .filter(record -> record.getTotalScore() >= questionnaire.getPassingScore())
                    .count();
            statistics.setPassedCount(passedCount);
            statistics.setPassRate((double) passedCount / completedCount);
            
            // 补充计算未通过人数
            statistics.setFailedCount(completedCount - passedCount);
        } else {
            // 如果没有设置及格分，则默认所有完成的测评都算通过
            statistics.setPassedCount(completedCount);
            statistics.setPassRate(completedCount > 0 ? 1.0 : 0.0);
            statistics.setFailedCount(0L);
        }

        // 计算最高分和最低分
        if (completedCount > 0) {
            Integer highestScore = recordRepository.findByQuestionnaireIdAndStatus(questionnaireId,
                    AssessmentRecord.AssessmentStatus.COMPLETED)
                    .stream()
                    .mapToInt(AssessmentRecord::getTotalScore)
                    .max()
                    .orElse(0);
            
            Integer lowestScore = recordRepository.findByQuestionnaireIdAndStatus(questionnaireId,
                    AssessmentRecord.AssessmentStatus.COMPLETED)
                    .stream()
                    .mapToInt(AssessmentRecord::getTotalScore)
                    .min()
                    .orElse(0);
            
            statistics.setHighestScore(highestScore);
            statistics.setLowestScore(lowestScore);
        } else {
            statistics.setHighestScore(0);
            statistics.setLowestScore(0);
        }

        return statistics;
    }

    /**
     * 计算题目得分
     */
    private int calculateScore(AssessmentQuestion question, List<Long> selectedOptions, Map<Long, QuestionOption> optionMap) {
        int score = 0;
        for (Long optionId : selectedOptions) {
            QuestionOption option = optionMap.get(optionId);
            if (option != null && option.getQuestion().getId().equals(question.getId())) {
                score += option.getScore();
            }
        }
        return score;
    }

    /**
     * 转换为测评结果响应DTO
     */
    private AssessmentResultResponse convertToAssessmentResultResponse(AssessmentRecord record) {
        AssessmentResultResponse response = new AssessmentResultResponse();
        response.setId(record.getId());
        response.setTotalScore(record.getTotalScore());
        response.setCreateTime(record.getCreateTime());

        // 设置是否通过
        Integer passingScore = record.getQuestionnaire().getPassingScore();
        response.setPassed(passingScore == null || record.getTotalScore() >= passingScore);

        // 设置问卷信息
        response.setQuestionnaire(convertToQuestionnaireResponse(record.getQuestionnaire()));

        // 设置答题详情
        List<AssessmentResultResponse.AnswerResultResponse> answers = record.getAnswers().stream()
                .map(this::convertToAnswerResultResponse)
                .collect(Collectors.toList());
        response.setAnswers(answers);

        return response;
    }

    /**
     * 转换为问卷响应DTO
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

        List<QuestionnaireResponse.QuestionResponse> questions = questionnaire.getQuestions().stream()
                .sorted(Comparator.comparing(AssessmentQuestion::getSortOrder))
                .map(this::convertToQuestionResponse)
                .collect(Collectors.toList());
        response.setQuestions(questions);

        return response;
    }

    /**
     * 转换为题目响应DTO
     */
    private QuestionnaireResponse.QuestionResponse convertToQuestionResponse(AssessmentQuestion question) {
        QuestionnaireResponse.QuestionResponse response = new QuestionnaireResponse.QuestionResponse();
        response.setId(question.getId());
        response.setQuestionText(question.getQuestionText());
        response.setQuestionType(question.getQuestionType().name());
        response.setScore(question.getScore());
        response.setSortOrder(question.getSortOrder());

        List<QuestionnaireResponse.OptionResponse> options = question.getOptions().stream()
                .sorted(Comparator.comparing(QuestionOption::getSortOrder))
                .map(this::convertToOptionResponse)
                .collect(Collectors.toList());
        response.setOptions(options);

        return response;
    }

    /**
     * 转换为选项响应DTO
     */
    private QuestionnaireResponse.OptionResponse convertToOptionResponse(QuestionOption option) {
        QuestionnaireResponse.OptionResponse response = new QuestionnaireResponse.OptionResponse();
        response.setId(option.getId());
        response.setOptionText(option.getOptionText());
        response.setScore(option.getScore());
        response.setSortOrder(option.getSortOrder());
        return response;
    }

    /**
     * 转换为答题结果响应DTO
     */
    private AssessmentResultResponse.AnswerResultResponse convertToAnswerResultResponse(AnswerRecord answer) {
        AssessmentResultResponse.AnswerResultResponse response = new AssessmentResultResponse.AnswerResultResponse();
        response.setQuestionId(answer.getQuestion().getId());
        response.setScore(answer.getScore());

        // 设置选中的选项
        List<Long> selectedOptions = Arrays.stream(answer.getSelectedOptionIds().split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        response.setSelectedOptions(selectedOptions);

        // 设置正确的选项（得分最高的选项）
        List<Long> correctOptions = answer.getQuestion().getOptions().stream()
                .filter(option -> option.getScore() > 0)
                .map(QuestionOption::getId)
                .collect(Collectors.toList());
        response.setCorrectOptions(correctOptions);

        return response;
    }
} 