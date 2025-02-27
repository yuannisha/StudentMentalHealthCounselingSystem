-- 创建心理测评问卷表
CREATE TABLE assessment_questionnaire (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    total_score INT,
    passing_score INT,
    status ENUM ('DRAFT','PUBLISHED','ARCHIVED') NOT NULL DEFAULT 'DRAFT', -- DRAFT, PUBLISHED, ARCHIVED
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建测评题目表
CREATE TABLE assessment_question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    questionnaire_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    question_type ENUM ('SINGLE_CHOICE', 'MULTIPLE_CHOICE') NOT NULL DEFAULT 'SINGLE_CHOICE',
    score INT NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    FOREIGN KEY (questionnaire_id) REFERENCES assessment_questionnaire(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建题目选项表
CREATE TABLE question_option (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    option_text VARCHAR(255) NOT NULL,
    score INT NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    FOREIGN KEY (question_id) REFERENCES assessment_question(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建用户测评记录表
CREATE TABLE assessment_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    questionnaire_id BIGINT NOT NULL,
    total_score INT NOT NULL DEFAULT 0,
    status ENUM ('IN_PROGRESS','COMPLETED') NOT NULL, -- IN_PROGRESS, COMPLETED
    start_time DATETIME NOT NULL,
    complete_time DATETIME,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (questionnaire_id) REFERENCES assessment_questionnaire(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建用户答题记录表
CREATE TABLE answer_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assessment_record_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    selected_option_ids VARCHAR(255) NOT NULL, -- 存储选中的选项ID，多个ID用逗号分隔
    score INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    FOREIGN KEY (assessment_record_id) REFERENCES assessment_record(id),
    FOREIGN KEY (question_id) REFERENCES assessment_question(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; 