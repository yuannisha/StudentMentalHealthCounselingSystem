package com.mentalhealth.counseling.controller;

import com.mentalhealth.counseling.dto.assessment.QuestionnaireRequest;
import com.mentalhealth.counseling.dto.assessment.QuestionnaireResponse;
import com.mentalhealth.counseling.entity.AssessmentQuestionnaire.QuestionnaireStatus;
import com.mentalhealth.counseling.service.AssessmentQuestionnaireService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 问卷管理控制器
 */
@RestController
@RequestMapping("/api/questionnaires")
@RequiredArgsConstructor
public class AssessmentQuestionnaireController {

    private final AssessmentQuestionnaireService questionnaireService;

    /**
     * 创建问卷
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuestionnaireResponse> createQuestionnaire(
            @RequestBody @Valid QuestionnaireRequest request
    ) {
        return ResponseEntity.ok(questionnaireService.createQuestionnaire(request));
    }

    /**
     * 更新问卷
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuestionnaireResponse> updateQuestionnaire(
            @PathVariable Long id,
            @RequestBody @Valid QuestionnaireRequest request
    ) {
        return ResponseEntity.ok(questionnaireService.updateQuestionnaire(id, request));
    }

    /**
     * 获取问卷详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<QuestionnaireResponse> getQuestionnaire(@PathVariable Long id) {
        return ResponseEntity.ok(questionnaireService.getQuestionnaire(id));
    }

    /**
     * 分页查询问卷列表
     */
    @GetMapping
    public ResponseEntity<Page<QuestionnaireResponse>> getQuestionnaires(
            @RequestParam(required = false) List<QuestionnaireStatus> statuses,
            @RequestParam(required = false) QuestionnaireStatus status,
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        List<QuestionnaireStatus> statusList = new ArrayList<>();
        if (statuses != null && !statuses.isEmpty()) {
            statusList = statuses;
        } else if (status != null) {
            statusList = Arrays.asList(status);
        } else {
            // 默认查询所有状态的问卷
            statusList = Arrays.asList(QuestionnaireStatus.DRAFT, QuestionnaireStatus.PUBLISHED, QuestionnaireStatus.ARCHIVED);
        }
        
        return ResponseEntity.ok(questionnaireService.getQuestionnaires(statusList, keyword, pageable));
    }

    /**
     * 发布问卷
     */
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> publishQuestionnaire(@PathVariable Long id) {
        questionnaireService.publishQuestionnaire(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 归档问卷
     */
    @PutMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> archiveQuestionnaire(@PathVariable Long id) {
        questionnaireService.archiveQuestionnaire(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 删除问卷
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteQuestionnaire(@PathVariable Long id) {
        questionnaireService.deleteQuestionnaire(id);
        return ResponseEntity.ok().build();
    }
} 