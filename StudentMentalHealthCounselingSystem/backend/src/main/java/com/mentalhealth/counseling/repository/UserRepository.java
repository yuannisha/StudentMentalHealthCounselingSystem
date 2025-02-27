package com.mentalhealth.counseling.repository;

import com.mentalhealth.counseling.entity.User;
import com.mentalhealth.counseling.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口
 */
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByUsername(String username);
    
    Optional<User> findByPhone(String phone);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByPhone(String phone);
    
    boolean existsByEmail(String email);
    
    List<User> findByRole(UserRole role);
} 