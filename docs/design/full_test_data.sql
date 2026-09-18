-- Graduation Defense Management System Test Data
USE defense_management;
-- 1. Years
INSERT IGNORE INTO defense_year (id, name, is_current, created_at)
VALUES (1, '2025 Summer', 1, NOW());
INSERT IGNORE INTO defense_year (id, name, is_current, created_at)
VALUES (2, '2024 Autumn', 0, NOW());
-- 2. Departments
INSERT IGNORE INTO department (id, name, admin_id)
VALUES (1, 'Software Engineering', 3);
INSERT IGNORE INTO department (id, name, admin_id)
VALUES (2, 'Computer Science', NULL);
-- 3. Teacher Signatures
UPDATE sys_user
SET signature = '/uploads/signatures/test_signature.png'
WHERE id IN (5, 6, 9);
-- 4. Defense Groups
INSERT IGNORE INTO defense_group (
        id,
        name,
        leader_id,
        year_id,
        department_id,
        location
    )
VALUES (10, 'Software Group 1', 5, 1, 1, 'Room 101');
INSERT IGNORE INTO defense_group (
        id,
        name,
        leader_id,
        year_id,
        department_id,
        location
    )
VALUES (11, 'Software Group 2', 6, 1, 1, 'Room 102');
-- 5. Students
-- Assign teacher02 (ID: 5) as advisor for students 2, 3
UPDATE student
SET advisor_id = 5,
    reviewer_id = 6,
    year_id = 1,
    department_id = 1
WHERE id IN (2, 3);
-- Assign teacher02 (ID: 5) as reviewer for students 4, 5
UPDATE student
SET advisor_id = 6,
    reviewer_id = 5,
    year_id = 1,
    department_id = 1
WHERE id IN (4, 5);
-- Final Defense Students
INSERT IGNORE INTO student (
        id,
        student_no,
        name,
        major,
        class_name,
        thesis_title,
        thesis_type,
        department_id,
        year_id,
        group_id,
        advisor_id,
        group_avg_score
    )
VALUES (
        101,
        'S202501',
        'John Doe',
        'SE',
        'Class 1',
        'Vue Management System',
        'PAPER',
        1,
        1,
        10,
        5,
        92.5
    ),
    (
        102,
        'S202502',
        'Jane Smith',
        'SE',
        'Class 1',
        'Crawler System',
        'DESIGN',
        1,
        1,
        10,
        6,
        85.0
    ),
    (
        103,
        'S202503',
        'Mike Wang',
        'SE',
        'Class 2',
        'Blockchain App',
        'PAPER',
        1,
        1,
        11,
        7,
        94.0
    );
UPDATE student
SET group_avg_score = 92.5
WHERE id = 101;
UPDATE student
SET group_avg_score = 94.0
WHERE id = 103;
-- 6. Group Scores
INSERT IGNORE INTO group_score (
        student_id,
        teacher_id,
        year_id,
        item1_score,
        item2_score,
        item3_score,
        total_score,
        created_at
    )
VALUES (101, 5, 1, 38, 28, 28, 94, NOW()),
    (101, 6, 1, 36, 26, 26, 88, NOW()),
    (103, 5, 1, 35, 25, 25, 85, NOW());
-- 7. AI Config
TRUNCATE TABLE ai_config;
INSERT INTO ai_config (id, api_key, paper_prompt, design_prompt)
VALUES (
        1,
        'sk-fake-key',
        'Paper comment template...',
        'Design comment template...'
    );
-- 8. Final Score
INSERT IGNORE INTO final_score (
        student_id,
        teacher_id,
        year_id,
        total_score,
        created_at
    )
VALUES (101, 11, 1, 93, NOW()),
    (103, 11, 1, 95, NOW());