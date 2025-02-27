package com.mentalhealth.counseling.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String requestURI = request.getRequestURI();
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;
        
        // 检查是否与测评相关的API
        boolean isAssessmentRelated = requestURI.contains("/api/assessment/") || requestURI.contains("/api/questionnaire/");
        boolean isAnswerSubmit = requestURI.contains("/api/assessment/answers");
        boolean isResultAccess = requestURI.contains("/api/assessment/results");
        
        // 添加请求来源分析，特别关注测评相关请求
        String referer = request.getHeader("Referer");
        String origin = request.getHeader("Origin");
        String userAgent = request.getHeader("User-Agent");
        
        boolean isFromAssessmentPage = referer != null && 
                (referer.contains("/student/assessment/") || referer.contains("/assessment-result/"));
        
        // 记录请求的更多信息，帮助调试
        if (isAssessmentRelated) {
            log.info("测评相关请求: URI='{}', 提交={}, 结果={}, Referer='{}', Origin='{}', 方法='{}'", 
                    requestURI, isAnswerSubmit, isResultAccess, referer, origin, request.getMethod());
        }
        
        // 处理预检请求
        if (request.getMethod().equals("OPTIONS")) {
            log.debug("收到OPTIONS预检请求: URI='{}', Origin='{}'", requestURI, origin);
            // 让预检请求通过，由Spring Security处理CORS
            filterChain.doFilter(request, response);
            return;
        }
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (isAssessmentRelated) {
                log.warn("访问测评相关API [{}] 没有提供有效的Authorization头, Referer='{}', Origin='{}'", 
                        requestURI, referer, origin);
                
                // 如果是测评提交或结果访问，返回更明确的错误信息
                if ((isAnswerSubmit || isResultAccess) && isFromAssessmentPage) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"message\":\"您的会话已过期，请重新登录后继续操作\",\"success\":false}");
                    return;
                }
            }
            filterChain.doFilter(request, response);
            return;
        }

        try {
            jwt = authHeader.substring(7);
            username = jwtService.extractUsername(jwt);
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.debug("JWT验证中: 用户='{}', URI='{}', 方法='{}'", username, requestURI, request.getMethod());
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    if (isAssessmentRelated) {
                        if (isAnswerSubmit) {
                            log.info("测评答案提交验证成功: 用户='{}', URI='{}', Referer='{}'", username, requestURI, referer);
                            
                            // 加入特殊的响应头，确保前端能正确识别授权状态
                            response.setHeader("X-Auth-Status", "valid");
                            response.setHeader("X-Auth-Username", username);
                        } else if (isResultAccess) {
                            log.info("测评结果访问验证成功: 用户='{}', URI='{}', Referer='{}'", username, requestURI, referer);
                            
                            // 加入特殊的响应头，确保前端能正确识别授权状态
                            response.setHeader("X-Auth-Status", "valid");
                            response.setHeader("X-Auth-Username", username);
                        } else {
                            log.info("测评API访问验证成功: 用户='{}', URI='{}', Referer='{}'", username, requestURI, referer);
                        }
                    }
                } else {
                    if (isAssessmentRelated) {
                        log.warn("测评API访问JWT无效: 用户='{}', URI='{}', Referer='{}'", username, requestURI, referer);
                        
                        // 特殊处理测评提交和结果访问
                        if (isAnswerSubmit || isResultAccess) {
                            log.error("关键测评操作JWT失效: 提交={}, 结果={}, 用户='{}', URI='{}', Referer='{}'", 
                                      isAnswerSubmit, isResultAccess, username, requestURI, referer);
                            
                            // 返回更友好的错误信息
                            if (isFromAssessmentPage) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json;charset=UTF-8");
                                response.setHeader("X-Auth-Status", "expired");
                                response.getWriter().write("{\"code\":401,\"message\":\"您的登录已过期，请重新登录后继续，您的测评数据不会丢失\",\"success\":false}");
                                return;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("JWT处理异常: URI='{}', Referer='{}', 错误='{}'", requestURI, referer, e.getMessage());
            if (isAssessmentRelated) {
                // 如果是测评相关API，记录更详细的错误信息并返回友好错误信息
                log.error("测评API JWT处理失败: 提交={}, 结果={}, 请求URI='{}', Referer='{}', 错误='{}'", 
                          isAnswerSubmit, isResultAccess, requestURI, referer, e.getMessage());
                
                if ((isAnswerSubmit || isResultAccess) && isFromAssessmentPage) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.setHeader("X-Auth-Status", "error");
                    response.getWriter().write("{\"code\":401,\"message\":\"授权验证失败，请重新登录\",\"success\":false,\"error\":\"" + e.getMessage() + "\"}");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
} 