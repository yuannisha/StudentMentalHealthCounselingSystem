-- 添加超级管理员账户
INSERT INTO users (
    username,
    password,
    real_name,
    phone,
    email,
    role,
    enabled,
    create_time,
    update_time
) VALUES (
    'admin',
    '$2a$10$rAZ0i4mGZpXWDkHGQkUQxOPAi6KUqZW1QYN.qQQaFS9rQqj2VXmHi', -- 密码：admin123
    '系统管理员',
    '13800000000',
    'admin@system.com',
    'ADMIN',
    true,
    NOW(),
    NOW()
); 