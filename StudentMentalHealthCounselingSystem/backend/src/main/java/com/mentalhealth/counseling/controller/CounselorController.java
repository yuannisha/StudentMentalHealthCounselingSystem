package com.mentalhealth.counseling.controller;

import com.mentalhealth.counseling.dto.UserProfileResponse;
import com.mentalhealth.counseling.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 咨询师控制器
 */
@RestController
@RequestMapping("/api/counselor")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COUNSELOR')")
public class CounselorController {

    private final UserService userService;

    /**
     * 获取学生列表（供咨询师使用）
     */
    @GetMapping("/students")
    public ResponseEntity<Page<UserProfileResponse>> getStudentsForCounselor(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(userService.getStudents(page, size, keyword));
    }
} 