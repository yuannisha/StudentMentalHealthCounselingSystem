package com.mentalhealth.counseling.service;

import com.mentalhealth.counseling.dto.AuthenticationRequest;
import com.mentalhealth.counseling.dto.AuthenticationResponse;
import com.mentalhealth.counseling.dto.RegisterRequest;
import com.mentalhealth.counseling.entity.User;
import com.mentalhealth.counseling.repository.UserRepository;
import com.mentalhealth.counseling.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * 用户注册
     */
    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        log.info("开始注册用户: {}", request.getUsername());
        
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("用户名已存在: {}", request.getUsername());
            throw new IllegalArgumentException("用户名已存在");
        }
        
        // 检查手机号是否已存在
        if (userRepository.existsByPhone(request.getPhone())) {
            log.warn("手机号已被注册: {}", request.getPhone());
            throw new IllegalArgumentException("手机号已被注册");
        }
        
        // 检查邮箱是否已存在
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            log.warn("邮箱已被注册: {}", request.getEmail());
            throw new IllegalArgumentException("邮箱已被注册");
        }

        var user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .realName(request.getRealName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .role(request.getRole())
                .university(request.getUniversity())
                .college(request.getCollege())
                .major(request.getMajor())
                .studentId(request.getStudentId())
                .enabled(true)
                .build();

        var savedUser = userRepository.save(user);
        log.info("用户注册成功: {}", savedUser.getUsername());
        
        var jwtToken = jwtService.generateToken(user);
        
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .role(savedUser.getRole())
                .build();
    }

    /**
     * 用户登录
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("用户开始登录: {}", request.getUsername());
        
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            log.warn("用户登录失败，用户名或密码错误: {}", request.getUsername());
            throw new BadCredentialsException("用户名或密码错误");
        }
        
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.error("用户不存在: {}", request.getUsername());
                    return new IllegalArgumentException("用户不存在");
                });

        if (!user.isEnabled()) {
            log.warn("账号已被禁用: {}", request.getUsername());
            throw new IllegalArgumentException("账号已被禁用");
        }
                
        var jwtToken = jwtService.generateToken(user);
        log.info("用户登录成功: {}", user.getUsername());
        
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}