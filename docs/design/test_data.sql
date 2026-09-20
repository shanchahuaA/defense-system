-- ============================================
-- 测试数据插入脚本
-- ============================================
SET NAMES utf8mb4;
USE defense_management;
-- ============================================
-- 1. 插入5个学院
-- ============================================
INSERT INTO department (name)
VALUES ('计算机与信息学院'),
    ('机械与汽车工程学院'),
    ('经济管理学院'),
    ('外国语学院'),
    ('艺术设计学院');
-- ============================================
-- 2. 插入10个教师 (密码都是123456)
-- ============================================
-- 第一批5个教师属于计算机与信息学院(id=1)
INSERT INTO sys_user (
        username,
        password,
        name,
        teacher_no,
        role,
        department_id
    )
VALUES (
        'teacher01',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
        '张三',
        'T001',
        'TEACHER',
        1
    ),
    (
        'teacher02',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
        '李四',
        'T002',
        'TEACHER',
        1
    ),
    (
        'teacher03',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
        '王五',
        'T003',
        'TEACHER',
        1
    ),
    (
        'teacher04',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
        '赵六',
        'T004',
        'TEACHER',
        1
    ),
    (
        'teacher05',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
        '孙七',
        'T005',
        'TEACHER',
        1
    );
-- 第二批5个教师分布在其他学院
INSERT INTO sys_user (
        username,
        password,
        name,
        teacher_no,
        role,
        department_id
    )
VALUES (
        'teacher06',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
        '周八',
        'T006',
        'TEACHER',
        2
    ),
    (
        'teacher07',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
        '吴九',
        'T007',
        'TEACHER',
        2
    ),
    (
        'teacher08',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
        '郑十',
        'T008',
        'TEACHER',
        3
    ),
    (
        'teacher09',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
        '冯十一',
        'T009',
        'TEACHER',
        4
    ),
    (
        'teacher10',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
        '陈十二',
        'T010',
        'TEACHER',
        5
    );
-- ============================================
-- 3. 插入10个学生
-- 注意：year_id=1 对应默认答辩年份(2025年夏答辩季)
-- ============================================
-- 5个论文类型学生(计算机学院)
INSERT INTO student (
        student_no,
        name,
        class_name,
        department_id,
        thesis_type,
        title,
        year_id,
        advisor_id
    )
VALUES (
        'S202101',
        '王小明',
        '软件2101',
        1,
        'PAPER',
        '基于Spring Boot的Web应用开发研究',
        1,
        2
    ),
    (
        'S202102',
        '李小红',
        '软件2101',
        1,
        'PAPER',
        '深度学习在图像识别中的应用',
        1,
        2
    ),
    (
        'S202103',
        '张小刚',
        '软件2102',
        1,
        'PAPER',
        '微服务架构在电商系统中的实践',
        1,
        3
    ),
    (
        'S202104',
        '刘小美',
        '软件2102',
        1,
        'DESIGN',
        'Vue.js前端框架应用与实践',
        1,
        3
    ),
    (
        'S202105',
        '陈小华',
        '软件2103',
        1,
        'DESIGN',
        'React Native移动应用开发',
        1,
        4
    );
-- 3个学生(机械学院)
INSERT INTO student (
        student_no,
        name,
        class_name,
        department_id,
        thesis_type,
        title,
        year_id,
        advisor_id
    )
VALUES (
        'S202201',
        '黄小龙',
        '机械2101',
        2,
        'PAPER',
        '智能制造技术在汽车行业的应用研究',
        1,
        7
    ),
    (
        'S202202',
        '周小燕',
        '机械2101',
        2,
        'DESIGN',
        '自动化生产线设计与实现',
        1,
        7
    ),
    (
        'S202203',
        '吴小波',
        '机械2102',
        2,
        'PAPER',
        '新能源汽车电池管理系统设计',
        1,
        8
    );
-- 2个学生(经管学院)
INSERT INTO student (
        student_no,
        name,
        class_name,
        department_id,
        thesis_type,
        title,
        year_id,
        advisor_id
    )
VALUES (
        'S202301',
        '郑小芳',
        '会计2101',
        3,
        'PAPER',
        '企业财务风险管理研究',
        1,
        9
    ),
    (
        'S202302',
        '林小杰',
        '会计2102',
        3,
        'PAPER',
        '数字经济时代企业转型策略分析',
        1,
        9
    );
-- ============================================
-- 查询验证
-- ============================================
SELECT '学院数量:' AS '统计',
    COUNT(*) AS '数量'
FROM department
UNION ALL
SELECT '教师数量:',
    COUNT(*)
FROM sys_user
WHERE role = 'TEACHER'
UNION ALL
SELECT '学生数量:',
    COUNT(*)
FROM student;