package com.mentalhealth.counseling.controller;

import com.mentalhealth.counseling.dto.assessment.AssessmentResultResponse;
import com.mentalhealth.counseling.dto.assessment.AssessmentSubmitRequest;
import com.mentalhealth.counseling.dto.assessment.QuestionnaireStatistics;
import com.mentalhealth.counseling.entity.User;
import com.mentalhealth.counseling.service.AssessmentRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 测评记录控制器
 */
@RestController
@RequestMapping("/api/assessment")
@RequiredArgsConstructor
@Slf4j
public class AssessmentRecordController {

    private final AssessmentRecordService assessmentRecordService;

    /**
     * 提交测评答案
     */
    @PostMapping("/answers")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AssessmentResultResponse> submitAnswers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid AssessmentSubmitRequest request
    ) {
        try {
            if (userDetails == null) {
                log.warn("提交测评答案时未找到认证用户");
                return ResponseEntity.status(401).build();
            }
            
            User user = (User) userDetails;
            log.info("用户[{}]提交测评答案，问卷ID: {}, 答案数量: {}", 
                    user.getUsername(), request.getQuestionnaireId(), 
                    request.getAnswers().size());
            
            AssessmentResultResponse result = assessmentRecordService.submitAnswers(
                    user.getId(),
                    request
            );
            
            log.info("用户[{}]成功提交测评答案，生成结果ID: {}, 得分: {}",
                    user.getUsername(), result.getId(), result.getTotalScore());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("提交测评答案时发生错误", e);
            throw e;
        }
    }

    /**
     * 获取测评结果
     */
    @GetMapping("/results/{recordId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AssessmentResultResponse> getAssessmentResult(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long recordId
    ) {
        return ResponseEntity.ok(assessmentRecordService.getAssessmentResult(
                ((User) userDetails).getId(),
                recordId
        ));
    }

    /**
     * 获取测评历史
     */
    @GetMapping("/results")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Page<AssessmentResultResponse>> getAssessmentHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable
    ) {
        return ResponseEntity.ok(assessmentRecordService.getAssessmentHistory(
                ((User) userDetails).getId(),
                pageable
        ));
    }

    /**
     * 获取特定用户的测评历史（管理员或咨询师使用）
     */
    @GetMapping("/user/{userId}/results")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<Page<AssessmentResultResponse>> getUserAssessmentHistory(
            @PathVariable Long userId,
            Pageable pageable
    ) {
        log.info("管理员或咨询师查询用户ID[{}]的测评历史", userId);
        return ResponseEntity.ok(assessmentRecordService.getAssessmentHistory(
                userId,
                pageable
        ));
    }

    /**
     * 获取问卷统计数据
     */
    @GetMapping("/questionnaires/{questionnaireId}/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuestionnaireStatistics> getQuestionnaireStatistics(
            @PathVariable Long questionnaireId
    ) {
        return ResponseEntity.ok(assessmentRecordService.getQuestionnaireStatistics(questionnaireId));
    }
} 