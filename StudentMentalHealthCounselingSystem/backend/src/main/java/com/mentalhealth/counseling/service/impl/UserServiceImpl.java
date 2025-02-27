package com.mentalhealth.counseling.service.impl;

import com.mentalhealth.counseling.dto.ChangePasswordRequest;
import com.mentalhealth.counseling.dto.UserProfileRequest;
import com.mentalhealth.counseling.dto.UserProfileResponse;
import com.mentalhealth.counseling.entity.User;
import com.mentalhealth.counseling.entity.UserRole;
import com.mentalhealth.counseling.repository.UserRepository;
import com.mentalhealth.counseling.service.UserService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserProfileResponse getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return convertToUserProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserProfile(String username, UserProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 检查手机号是否被其他用户使用
        if (!user.getPhone().equals(request.getPhone()) &&
                userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("手机号已被使用");
        }

        // 检查邮箱是否被其他用户使用
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("邮箱已被使用");
        }

        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setUniversity(request.getUniversity());
        user.setCollege(request.getCollege());
        user.setMajor(request.getMajor());
        user.setStudentId(request.getStudentId());

        User savedUser = userRepository.save(user);
        return convertToUserProfileResponse(savedUser);
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadCredentialsException("旧密码不正确");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    
    @Override
    public Page<UserProfileResponse> getUsersByRole(String search, UserRole role, Pageable pageable) {
        return findUsersBySearchAndRole(search, role, pageable)
                .map(this::convertToUserProfileResponse);
    }
    
    @Override
    public Page<UserProfileResponse> getStudents(int page, int size, String keyword) {
        Specification<User> spec = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 添加角色条件：只查询学生
            predicates.add(builder.equal(root.get("role"), UserRole.STUDENT));
            
            // 添加关键词搜索条件
            if (StringUtils.hasText(keyword)) {
                String likePattern = "%" + keyword.trim() + "%";
                List<Predicate> orPredicates = new ArrayList<>();
                
                // 搜索用户名、真实姓名、邮箱、电话、学号等字段
                orPredicates.add(builder.like(root.get("username"), likePattern));
                orPredicates.add(builder.like(root.get("realName"), likePattern));
                orPredicates.add(builder.like(root.get("email"), likePattern));
                orPredicates.add(builder.like(root.get("phone"), likePattern));
                orPredicates.add(builder.like(root.get("studentId"), likePattern));
                orPredicates.add(builder.like(root.get("university"), likePattern));
                orPredicates.add(builder.like(root.get("college"), likePattern));
                orPredicates.add(builder.like(root.get("major"), likePattern));
                
                // 将所有or条件合并
                predicates.add(builder.or(orPredicates.toArray(new Predicate[0])));
            }
            
            return builder.and(predicates.toArray(new Predicate[0]));
        };
        
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return userRepository.findAll(spec, pageable).map(this::convertToUserProfileResponse);
    }

    // 现有的方法：按搜索词和角色查找用户
    private Page<User> findUsersBySearchAndRole(String search, UserRole role, Pageable pageable) {
        Specification<User> spec = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 添加角色条件
            if (role != null) {
                predicates.add(builder.equal(root.get("role"), role));
            }
            
            // 添加搜索条件
            if (StringUtils.hasText(search)) {
                String likePattern = "%" + search.trim() + "%";
                List<Predicate> orPredicates = new ArrayList<>();
                
                // 搜索用户名、真实姓名、邮箱、电话等字段
                orPredicates.add(builder.like(root.get("username"), likePattern));
                orPredicates.add(builder.like(root.get("realName"), likePattern));
                orPredicates.add(builder.like(root.get("email"), likePattern));
                orPredicates.add(builder.like(root.get("phone"), likePattern));
                
                // 将所有or条件合并
                predicates.add(builder.or(orPredicates.toArray(new Predicate[0])));
            }
            
            return builder.and(predicates.toArray(new Predicate[0]));
        };
        
        return userRepository.findAll(spec, pageable);
    }

    private UserProfileResponse convertToUserProfileResponse(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setUniversity(user.getUniversity());
        response.setCollege(user.getCollege());
        response.setMajor(user.getMajor());
        response.setStudentId(user.getStudentId());
        return response;
    }
} 