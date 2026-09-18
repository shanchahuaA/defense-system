-- ============================================
-- 测试数据 - 答辩评分功能测试
-- 标记：所有测试数据都包含 _TEST 标记或备注
-- 使用后请执行本文件末尾的删除脚本
-- ============================================
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
USE defense_management;
-- ============================================
-- 1. 创建答辩分组（计算机学院2个分组）
-- 根据实际数据：department_id=1是计算机学院
-- 教师：李四(5), 王五(6), 赵六(7), 孙七(8)
-- ============================================
INSERT INTO defense_group (
        name,
        department_id,
        leader_id,
        year_id,
        adjustment_factor
    )
VALUES ('计算机学院第1答辩组_TEST', 1, 5, 1, 1.000),
    -- 组长是李四(id=5)
    ('计算机学院第2答辩组_TEST', 1, 7, 1, 1.000);
-- 组长是赵六(id=7)
-- 获取刚创建的分组ID
SET @group1_id = LAST_INSERT_ID();
SET @group2_id = @group1_id + 1;
-- ============================================
-- 2. 将教师分配到答辩分组
-- ============================================
-- 第1组：李四(组长5)、王五(6)
INSERT INTO group_teacher (group_id, teacher_id, teacher_order)
VALUES (@group1_id, 5, 1),
    -- 李四
    (@group1_id, 6, 2);
-- 王五
-- 第2组：赵六(组长7)、孙七(8)
INSERT INTO group_teacher (group_id, teacher_id, teacher_order)
VALUES (@group2_id, 7, 1),
    -- 赵六
    (@group2_id, 8, 2);
-- 孙七
-- ============================================
-- 3. 将学生分配到答辩分组
-- 学生ID：4=王小明, 5=李小红, 6=张小刚, 7=刘小美, 8=陈小华
-- ============================================
-- 第1组分配3个学生（论文类型）
UPDATE student
SET group_id = @group1_id
WHERE id IN (4, 5, 6);
-- 第2组分配2个学生（设计类型）
UPDATE student
SET group_id = @group2_id
WHERE id IN (7, 8);
-- ============================================
-- 4. 插入小组评分数据（测试用）
-- ============================================
-- 第1组教师给学生打分（论文类型，3项评分）
-- 李四(ID=5)给第1组学生打分
INSERT INTO group_score (
        student_id,
        teacher_id,
        year_id,
        item1_score,
        item2_score,
        item3_score,
        total_score
    )
VALUES (4, 5, 1, 35, 28, 27, 90),
    -- 王小明
    (5, 5, 1, 38, 26, 25, 89),
    -- 李小红
    (6, 5, 1, 36, 27, 25, 88);
-- 张小刚
-- 王五(ID=6)给第1组学生打分
INSERT INTO group_score (
        student_id,
        teacher_id,
        year_id,
        item1_score,
        item2_score,
        item3_score,
        total_score
    )
VALUES (4, 6, 1, 36, 27, 28, 91),
    -- 王小明
    (5, 6, 1, 37, 25, 26, 88),
    -- 李小红
    (6, 6, 1, 35, 28, 24, 87);
-- 张小刚
-- 第2组教师给学生打分（设计类型，6项评分）
-- 赵六(ID=7)给第2组学生打分
INSERT INTO group_score (
        student_id,
        teacher_id,
        year_id,
        item1_score,
        item2_score,
        item3_score,
        item4_score,
        item5_score,
        item6_score,
        total_score
    )
VALUES (7, 7, 1, 14, 13, 14, 23, 13, 14, 91),
    -- 刘小美
    (8, 7, 1, 13, 14, 13, 22, 14, 13, 89);
-- 陈小华
-- 孙七(ID=8)给第2组学生打分
INSERT INTO group_score (
        student_id,
        teacher_id,
        year_id,
        item1_score,
        item2_score,
        item3_score,
        item4_score,
        item5_score,
        item6_score,
        total_score
    )
VALUES (7, 8, 1, 13, 14, 13, 24, 14, 13, 91),
    -- 刘小美
    (8, 8, 1, 14, 13, 14, 21, 13, 14, 89);
-- 陈小华
-- ============================================
-- 5. 更新学生的指导和评阅成绩
-- ============================================
UPDATE student
SET advisor_score = 88,
    reviewer_score = 85
WHERE id = 4;
-- 王小明
UPDATE student
SET advisor_score = 85,
    reviewer_score = 82
WHERE id = 5;
-- 李小红
UPDATE student
SET advisor_score = 86,
    reviewer_score = 83
WHERE id = 6;
-- 张小刚
UPDATE student
SET advisor_score = 90,
    reviewer_score = 87
WHERE id = 7;
-- 刘小美
UPDATE student
SET advisor_score = 87,
    reviewer_score = 84
WHERE id = 8;
-- 陈小华
-- ============================================
-- 验证查询
-- ============================================
SELECT '答辩分组数量(带_TEST):' AS info,
    COUNT(*) AS count
FROM defense_group
WHERE name LIKE '%_TEST';
SELECT '小组教师分配数量:' AS info,
    COUNT(*) AS count
FROM group_teacher
WHERE group_id >= @group1_id;
SELECT '已分组学生数量:' AS info,
    COUNT(*) AS count
FROM student
WHERE group_id IS NOT NULL;
SELECT '小组评分记录数量:' AS info,
    COUNT(*) AS count
FROM group_score;
-- ============================================
-- ===== 删除测试数据脚本 =====
-- 测试完成后执行以下语句删除测试数据
-- ============================================
/*
 -- 删除测试评分
 DELETE FROM group_score WHERE year_id = 1;
 DELETE FROM final_score WHERE year_id = 1;
 
 -- 清除学生分组和成绩
 UPDATE student SET group_id = NULL, advisor_score = NULL, reviewer_score = NULL WHERE id IN (4,5,6,7,8);
 
 -- 删除分组-教师关联
 DELETE gt FROM group_teacher gt INNER JOIN defense_group dg ON gt.group_id = dg.id WHERE dg.name LIKE '%_TEST';
 
 -- 删除测试分组
 DELETE FROM defense_group WHERE name LIKE '%_TEST';
 
 SELECT '测试数据已清除' AS status;
 */