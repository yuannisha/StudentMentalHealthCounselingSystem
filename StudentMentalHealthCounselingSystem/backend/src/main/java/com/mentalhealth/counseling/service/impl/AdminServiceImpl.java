package com.mentalhealth.counseling.service.impl;

import com.mentalhealth.counseling.dto.UserProfileResponse;
import com.mentalhealth.counseling.entity.User;
import com.mentalhealth.counseling.entity.UserRole;
import com.mentalhealth.counseling.exception.ResourceNotFoundException;
import com.mentalhealth.counseling.repository.UserRepository;
import com.mentalhealth.counseling.service.AdminService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理员服务实现类
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    @Override
    public Page<UserProfileResponse> getUsers(String search, UserRole role, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(search)) {
                String searchTerm = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("username")), searchTerm),
                    cb.like(cb.lower(root.get("realName")), searchTerm),
                    cb.like(cb.lower(root.get("email")), searchTerm),
                    cb.like(cb.lower(root.get("phone")), searchTerm)
                ));
            }

            if (role != null) {
                predicates.add(cb.equal(root.get("role"), role));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return userRepository.findAll(spec, pageable)
                .map(this::convertToUserProfileResponse);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        // 不允许删除管理员账号
        if (user.getRole() == UserRole.ADMIN) {
            throw new IllegalArgumentException("不能删除管理员账号");
        }
        
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        // 不允许禁用管理员账号
        if (user.getRole() == UserRole.ADMIN) {
            throw new IllegalArgumentException("不能禁用管理员账号");
        }
        
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    private UserProfileResponse convertToUserProfileResponse(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setEnabled(user.isEnabled());
        response.setUniversity(user.getUniversity());
        response.setCollege(user.getCollege());
        response.setMajor(user.getMajor());
        response.setStudentId(user.getStudentId());
        response.setCreatedAt(user.getCreateTime().toString());
        response.setUpdatedAt(user.getUpdateTime().toString());
        return response;
    }
} 