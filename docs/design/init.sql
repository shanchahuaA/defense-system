-- ============================================
-- 福建农林大学答辩管理系统 - 数据库初始化脚本
-- ============================================
-- 创建数据库
CREATE DATABASE IF NOT EXISTS defense_management DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci;
USE defense_management;
-- ============================================
-- 1. 答辩年份表
-- ============================================
CREATE TABLE defense_year (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '年份名称，如2025年夏答辩季',
    is_current BOOLEAN DEFAULT FALSE COMMENT '是否当前年份',
    score_date DATE COMMENT '成绩表日期',
    evaluation_date DATE COMMENT '评定表日期',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '答辩年份表';
-- ============================================
-- 2. 院系表
-- ============================================
CREATE TABLE department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '院系名称',
    dean_signature_url VARCHAR(255) COMMENT '系主任签名图片路径',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '院系表';
-- ============================================
-- 3. 用户表（管理员+教师）
-- ============================================
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    teacher_no VARCHAR(20) COMMENT '教师编号',
    role ENUM('SUPER_ADMIN', 'DEPT_ADMIN', 'TEACHER') NOT NULL COMMENT '角色',
    department_id BIGINT COMMENT '所属院系',
    signature_url VARCHAR(255) COMMENT '签名图片路径',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE
    SET NULL
) COMMENT '用户表';
-- 添加院系管理员关联
ALTER TABLE department
ADD COLUMN admin_id BIGINT;
ALTER TABLE department
ADD CONSTRAINT fk_dept_admin FOREIGN KEY (admin_id) REFERENCES sys_user(id) ON DELETE
SET NULL;
-- ============================================
-- 4. 答辩小组表
-- ============================================
CREATE TABLE defense_group (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '小组名称',
    department_id BIGINT NOT NULL COMMENT '所属院系',
    leader_id BIGINT COMMENT '组长ID',
    year_id BIGINT NOT NULL COMMENT '答辩年份',
    adjustment_factor DECIMAL(5, 3) DEFAULT 1.000 COMMENT '调节系数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE CASCADE,
    FOREIGN KEY (leader_id) REFERENCES sys_user(id) ON DELETE
    SET NULL,
        FOREIGN KEY (year_id) REFERENCES defense_year(id) ON DELETE CASCADE
) COMMENT '答辩小组表';
-- ============================================
-- 5. 小组-教师关联表
-- ============================================
CREATE TABLE group_teacher (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    teacher_order INT DEFAULT 1 COMMENT '评委顺序（1-5）',
    FOREIGN KEY (group_id) REFERENCES defense_group(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    UNIQUE KEY uk_group_teacher (group_id, teacher_id)
) COMMENT '小组-教师关联表';
-- ============================================
-- 6. 学生表
-- ============================================
CREATE TABLE student (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_no VARCHAR(20) NOT NULL COMMENT '学号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    class_name VARCHAR(10) COMMENT '班级（数字，如1、2）',
    major VARCHAR(50) COMMENT '专业名称',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    department_id BIGINT NOT NULL COMMENT '所属院系',
    thesis_type ENUM('PAPER', 'DESIGN') NOT NULL DEFAULT 'PAPER' COMMENT '毕业类型：论文/设计',
    title VARCHAR(255) COMMENT '毕业题目',
    abstract_text TEXT COMMENT '摘要',
    advisor_id BIGINT COMMENT '指导教师ID',
    reviewer_id BIGINT COMMENT '评阅人ID',
    advisor_score INT COMMENT '指导教师评定成绩',
    reviewer_score INT COMMENT '评阅人评定成绩',
    group_id BIGINT COMMENT '所属答辩小组',
    year_id BIGINT NOT NULL COMMENT '答辩年份',
    defense_date DATE COMMENT '答辩日期',
    defense_location VARCHAR(100) COMMENT '答辩地点',
    ai_comment TEXT COMMENT 'AI生成评语',
    final_comment TEXT COMMENT '最终评语',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE CASCADE,
    FOREIGN KEY (advisor_id) REFERENCES sys_user(id) ON DELETE
    SET NULL,
        FOREIGN KEY (reviewer_id) REFERENCES sys_user(id) ON DELETE
    SET NULL,
        FOREIGN KEY (group_id) REFERENCES defense_group(id) ON DELETE
    SET NULL,
        FOREIGN KEY (year_id) REFERENCES defense_year(id) ON DELETE CASCADE,
        UNIQUE KEY uk_student_no_year (student_no, year_id)
) COMMENT '学生表';
-- ============================================
-- 7. 评分指标配置表
-- ============================================
CREATE TABLE score_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    thesis_type ENUM('PAPER', 'DESIGN') NOT NULL COMMENT '毕业类型',
    item_index INT NOT NULL COMMENT '分项序号（1-6）',
    item_name VARCHAR(100) NOT NULL COMMENT '分项名称',
    item_description TEXT COMMENT '分项具体内容',
    weight DECIMAL(3, 2) NOT NULL COMMENT '权重',
    max_score INT NOT NULL COMMENT '最高分',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_type_index (thesis_type, item_index)
) COMMENT '评分指标配置表';
-- 插入默认评分指标（论文类：3项）
INSERT INTO score_config (
        thesis_type,
        item_index,
        item_name,
        weight,
        max_score
    )
VALUES ('PAPER', 1, '论文质量', 0.40, 40),
    ('PAPER', 2, '答辩的自述报告', 0.30, 30),
    ('PAPER', 3, '回答问题的情况', 0.30, 30);
-- 插入默认评分指标（设计类：6项）
INSERT INTO score_config (
        thesis_type,
        item_index,
        item_name,
        weight,
        max_score
    )
VALUES ('DESIGN', 1, '设计质量1', 0.15, 15),
    ('DESIGN', 2, '设计质量2', 0.15, 15),
    ('DESIGN', 3, '设计质量3', 0.15, 15),
    ('DESIGN', 4, '答辩的自述报告', 0.25, 25),
    ('DESIGN', 5, '回答问题情况1', 0.15, 15),
    ('DESIGN', 6, '回答问题情况2', 0.15, 15);
-- ============================================
-- 8. 小组答辩评分表
-- ============================================
CREATE TABLE group_score (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL COMMENT '学生ID',
    teacher_id BIGINT NOT NULL COMMENT '评委ID',
    year_id BIGINT NOT NULL COMMENT '答辩年份',
    item1_score INT DEFAULT 0 COMMENT '分项1成绩',
    item2_score INT DEFAULT 0 COMMENT '分项2成绩',
    item3_score INT DEFAULT 0 COMMENT '分项3成绩',
    item4_score INT DEFAULT 0 COMMENT '分项4成绩（设计类）',
    item5_score INT DEFAULT 0 COMMENT '分项5成绩（设计类）',
    item6_score INT DEFAULT 0 COMMENT '分项6成绩（设计类）',
    total_score INT DEFAULT 0 COMMENT '合计成绩',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (year_id) REFERENCES defense_year(id) ON DELETE CASCADE,
    UNIQUE KEY uk_student_teacher_year (student_id, teacher_id, year_id)
) COMMENT '小组答辩评分表';
-- ============================================
-- 9. 大组答辩评分表
-- ============================================
CREATE TABLE final_score (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL COMMENT '学生ID（小组第一名）',
    teacher_id BIGINT NOT NULL COMMENT '评委ID',
    year_id BIGINT NOT NULL COMMENT '答辩年份',
    total_score INT DEFAULT 0 COMMENT '总评成绩（满分100）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (year_id) REFERENCES defense_year(id) ON DELETE CASCADE,
    UNIQUE KEY uk_student_teacher_year (student_id, teacher_id, year_id)
) COMMENT '大组答辩评分表';
-- ============================================
-- 10. 答辩记录表
-- ============================================
CREATE TABLE defense_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL COMMENT '学生ID',
    record_content TEXT COMMENT '答辩记录内容',
    recorder_id BIGINT COMMENT '记录人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    FOREIGN KEY (recorder_id) REFERENCES sys_user(id) ON DELETE
    SET NULL,
        UNIQUE KEY uk_student (student_id)
) COMMENT '答辩记录表';
-- ============================================
-- 11. AI配置表
-- ============================================
CREATE TABLE ai_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    api_key VARCHAR(255) COMMENT '千问API Key',
    paper_prompt TEXT COMMENT '论文评语提示词模板',
    design_prompt TEXT COMMENT '设计评语提示词模板',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT 'AI配置表';
-- 插入默认配置
INSERT INTO ai_config (paper_prompt, design_prompt)
VALUES (
        '请根据以下学生毕业论文的摘要，生成一段专业、客观的答辩小组评语（100-150字）：\n\n学生姓名：{{studentName}}\n论文题目：{{title}}\n论文摘要：{{abstract}}',
        '请根据以下学生毕业设计的摘要，生成一段专业、客观的答辩小组评语（100-150字）：\n\n学生姓名：{{studentName}}\n设计题目：{{title}}\n设计摘要：{{abstract}}'
    );
-- ============================================
-- 12. 文档模板表
-- ============================================
CREATE TABLE document_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    template_type VARCHAR(50) NOT NULL COMMENT '模板类型',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    file_path VARCHAR(255) NOT NULL COMMENT '文件路径',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_type (template_type)
) COMMENT '文档模板表';
-- ============================================
-- 初始化超级管理员账号
-- 密码：123456（BCrypt加密）
-- ============================================
INSERT INTO sys_user (username, password, name, role)
VALUES (
        'admin',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
        '超级管理员',
        'SUPER_ADMIN'
    );
-- ============================================
-- 初始化默认答辩年份
-- ============================================
INSERT INTO defense_year (name, is_current, score_date, evaluation_date)
VALUES ('2025年夏答辩季', TRUE, '2025-06-15', '2025-06-20');