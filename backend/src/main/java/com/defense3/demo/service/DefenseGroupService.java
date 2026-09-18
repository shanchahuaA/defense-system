package com.defense3.demo.service;

import com.defense3.demo.dto.DefenseGroupRequest;
import com.defense3.demo.dto.DefenseGroupResponse;
import com.defense3.demo.dto.StudentResponse;
import com.defense3.demo.entity.*;
import com.defense3.demo.exception.BusinessException;
import com.defense3.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 答辩小组服务
 */
@Service
@RequiredArgsConstructor
public class DefenseGroupService {

    private final DefenseGroupRepository groupRepository;
    private final GroupTeacherRepository groupTeacherRepository;
    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final DefenseYearRepository yearRepository;
    private final UserRepository userRepository;

    /**
     * 获取院系的答辩小组列表
     */
    public List<DefenseGroupResponse> findByDepartmentAndYear(Long departmentId, Long yearId) {
        List<DefenseGroup> groups = groupRepository.findByDepartmentIdAndYearId(departmentId, yearId);
        return groups.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * 获取教师所在的答辩小组
     */
    public List<DefenseGroupResponse> findByTeacher(Long teacherId, Long yearId) {
        List<GroupTeacher> groupTeachers = groupTeacherRepository.findByTeacherId(teacherId);
        List<DefenseGroup> groups = new ArrayList<>();

        for (GroupTeacher gt : groupTeachers) {
            groupRepository.findById(gt.getGroupId())
                    .filter(g -> yearId == null || yearId.equals(g.getYearId()))
                    .ifPresent(groups::add);
        }

        return groups.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * 获取组长所带小组的学生列表
     */
    public List<StudentResponse> findStudentsByLeader(Long leaderId, Long yearId) {
        // 查找该教师作为组长的答辩小组
        List<DefenseGroup> groups = groupRepository.findByLeaderId(leaderId);

        // 过滤年份
        if (yearId != null) {
            groups = groups.stream()
                    .filter(g -> yearId.equals(g.getYearId()))
                    .collect(Collectors.toList());
        }

        // 获取所有小组的学生
        List<Long> groupIds = groups.stream().map(DefenseGroup::getId).collect(Collectors.toList());
        if (groupIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Student> students = studentRepository.findByGroupIdIn(groupIds);
        return students.stream()
                .map(s -> {
                    String deptName = s.getDepartmentId() != null ? departmentRepository.findById(s.getDepartmentId())
                            .map(Department::getName).orElse(null) : null;
                    String advisorName = s.getAdvisorId() != null ? userRepository.findById(s.getAdvisorId())
                            .map(User::getName).orElse(null) : null;
                    String reviewerName = s.getReviewerId() != null ? userRepository.findById(s.getReviewerId())
                            .map(User::getName).orElse(null) : null;
                    String groupName = s.getGroupId() != null ? groupRepository.findById(s.getGroupId())
                            .map(DefenseGroup::getName).orElse(null) : null;
                    return StudentResponse.fromEntity(s, deptName, advisorName, reviewerName, groupName);
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取答辩小组
     */
    public DefenseGroupResponse findById(Long id) {
        DefenseGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new BusinessException("答辩小组不存在"));
        return convertToResponse(group);
    }

    /**
     * 创建答辩小组
     */
    @Transactional
    public DefenseGroupResponse create(DefenseGroupRequest request, Long yearId) {
        // 教师冲突检查
        validateTeacherConflicts(null, request, yearId);

        DefenseGroup group = new DefenseGroup();
        group.setName(request.getName());
        group.setDepartmentId(request.getDepartmentId());
        group.setYearId(yearId);
        group.setLeaderId(request.getLeaderId());
        group.setThesisType(request.getThesisType());
        group.setLocation(request.getLocation());
        group.setDefenseTime(request.getDefenseTime());

        group = groupRepository.save(group);

        // 验证组长不能是系管理员
        if (group.getLeaderId() != null) {
            User leader = userRepository.findById(group.getLeaderId())
                    .orElseThrow(() -> new BusinessException("组长不存在"));
            if (leader.getRole() == UserRole.DEPT_ADMIN) {
                throw new BusinessException("系管理员不能担任答辩组长");
            }
        }

        // 添加小组教师
        if (request.getTeacherIds() != null && !request.getTeacherIds().isEmpty()) {
            Long groupId = group.getId();
            List<Long> teacherIds = request.getTeacherIds().stream()
                    .distinct()
                    .collect(Collectors.toList());

            for (int i = 0; i < teacherIds.size(); i++) {
                GroupTeacher gt = new GroupTeacher();
                gt.setGroupId(groupId);
                gt.setTeacherId(teacherIds.get(i));
                gt.setTeacherOrder(i + 1);
                groupTeacherRepository.save(gt);
            }
        }

        return convertToResponse(group);
    }

    /**
     * 更新答辩小组
     */
    @Transactional
    public DefenseGroupResponse update(Long id, DefenseGroupRequest request) {
        DefenseGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new BusinessException("答辩小组不存在"));

        // 教师冲突检查
        validateTeacherConflicts(id, request, group.getYearId());

        group.setName(request.getName());
        group.setLeaderId(request.getLeaderId());
        group.setThesisType(request.getThesisType());
        group.setLocation(request.getLocation());
        group.setDefenseTime(request.getDefenseTime());

        group = groupRepository.save(group);

        // 验证组长不能是系管理员
        if (group.getLeaderId() != null) {
            User leader = userRepository.findById(group.getLeaderId())
                    .orElseThrow(() -> new BusinessException("组长不存在"));
            if (leader.getRole() == UserRole.DEPT_ADMIN) {
                throw new BusinessException("系管理员不能担任答辩组长");
            }
        }

        // 更新小组教师
        if (request.getTeacherIds() != null) {
            groupTeacherRepository.deleteByGroupId(id);
            // 过滤重复的教师ID
            List<Long> teacherIds = request.getTeacherIds().stream()
                    .distinct()
                    .collect(Collectors.toList());

            for (int i = 0; i < teacherIds.size(); i++) {
                GroupTeacher gt = new GroupTeacher();
                gt.setGroupId(id);
                gt.setTeacherId(teacherIds.get(i));
                gt.setTeacherOrder(i + 1);
                groupTeacherRepository.save(gt);
            }
        }

        return convertToResponse(group);
    }

    /**
     * 验证教师是否已经在该年份的其他小组中
     */
    private void validateTeacherConflicts(Long currentGroupId, DefenseGroupRequest request, Long yearId) {
        List<Long> otherGroupIds = groupRepository.findByYearId(yearId).stream()
                .map(DefenseGroup::getId)
                .filter(id -> !id.equals(currentGroupId))
                .collect(Collectors.toList());

        if (otherGroupIds.isEmpty())
            return;

        // 检查组长
        if (request.getLeaderId() != null) {
            if (isTeacherInGroups(request.getLeaderId(), otherGroupIds)) {
                User teacher = userRepository.findById(request.getLeaderId()).orElse(null);
                throw new BusinessException("教师 " + (teacher != null ? teacher.getName() : "") + " 已经在其他答辩小组中了");
            }
        }

        // 检查组员
        if (request.getTeacherIds() != null) {
            for (Long teacherId : request.getTeacherIds()) {
                if (isTeacherInGroups(teacherId, otherGroupIds)) {
                    User teacher = userRepository.findById(teacherId).orElse(null);
                    throw new BusinessException("教师 " + (teacher != null ? teacher.getName() : "") + " 已经在其他答辩小组中了");
                }
            }
        }
    }

    private boolean isTeacherInGroups(Long teacherId, List<Long> groupIds) {
        // 1. 检查是否在其它小组当组员
        if (groupTeacherRepository.existsByTeacherIdAndGroupIdIn(teacherId, groupIds)) {
            return true;
        }
        // 2. 检查是否在其它小组当组长
        for (Long gid : groupIds) {
            DefenseGroup g = groupRepository.findById(gid).orElse(null);
            if (g != null && teacherId.equals(g.getLeaderId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 开启大组答辩（系级）
     * 自动提取每个小组的第一名进入大组
     * 
     * 这里的逻辑是：
     * 1. 找到所有的普通答辩小组。
     * 2. 对于每个小组，它原本就有分配好的老师（组员教师）。
     * 3. 自动将该小组内，排名第一的学生自动“晋级”到大组答辩。
     * 4. 在系统中创建一个统一的“院系大组答辩”虚拟组。
     */
    @Transactional
    public void startUniversityDefense(Long departmentId, Long yearId) {
        // 1. 获取该院系该年份的所有普通小组
        List<DefenseGroup> allGroups = groupRepository.findByDepartmentIdAndYearId(departmentId, yearId).stream()
                .filter(g -> !g.getName().contains("大组答辩")) // 排除已存在的大组
                .collect(Collectors.toList());

        if (allGroups.isEmpty()) {
            throw new BusinessException("当前没有任何普通答辩小组，请先分配普通小组及教师");
        }

        // 2. 寻找或创建统一的大组（汇总所有晋级学生）
        String universityGroupName = "院系大组答辩 (汇总)";
        DefenseGroup universityGroup = groupRepository
                .findByNameAndDepartmentIdAndYearId(universityGroupName, departmentId, yearId)
                .orElseGet(() -> {
                    DefenseGroup g = new DefenseGroup();
                    g.setName(universityGroupName);
                    g.setDepartmentId(departmentId);
                    g.setYearId(yearId);
                    g.setLocation("行政楼大厅");
                    g.setDefenseTime("待定");
                    return groupRepository.save(g);
                });

        // 3. 核心逻辑：遍历每个普通组，取出该组的第一名
        // 这里的排序依据是小组平均分
        List<Student> studentsToPromote = new ArrayList<>();
        for (DefenseGroup group : allGroups) {
            // 获取该组所有学生并按平均分降序
            List<Student> students = studentRepository.findByGroupIdOrderByAvgScoreDesc(group.getId());
            if (!students.isEmpty()) {
                // 取第一名
                studentsToPromote.add(students.get(0));
            }
        }

        if (studentsToPromote.isEmpty()) {
            throw new BusinessException("各小组尚未评分，无法提取第一名");
        }

        // 4. 将晋级学生移动到统一的大组中
        for (Student student : studentsToPromote) {
            student.setGroupId(universityGroup.getId());
            // 进入大组视作重新抽签，清除原顺序
            student.setDefenseOrder(null);
            studentRepository.save(student);
        }
    }

    /**
     * 删除答辩小组
     */
    @Transactional
    public void delete(Long id) {
        if (!groupRepository.existsById(id)) {
            throw new BusinessException("答辩小组不存在");
        }

        // 检查是否有学生
        if (studentRepository.countByGroupId(id) > 0) {
            throw new BusinessException("该小组下存在学生，无法删除");
        }

        groupTeacherRepository.deleteByGroupId(id);
        groupRepository.deleteById(id);
    }

    private DefenseGroupResponse convertToResponse(DefenseGroup group) {
        String deptName = null;
        String yearName = null;
        String leaderName = null;

        if (group.getDepartmentId() != null) {
            deptName = departmentRepository.findById(group.getDepartmentId())
                    .map(Department::getName).orElse(null);
        }
        if (group.getYearId() != null) {
            yearName = yearRepository.findById(group.getYearId())
                    .map(DefenseYear::getName).orElse(null);
        }
        if (group.getLeaderId() != null) {
            leaderName = userRepository.findById(group.getLeaderId())
                    .map(User::getName).orElse(null);
        }

        // 获取小组教师
        List<GroupTeacher> groupTeachers = groupTeacherRepository.findByGroupIdOrderByTeacherOrder(group.getId());
        List<DefenseGroupResponse.TeacherInfo> teachers = groupTeachers.stream()
                .map(gt -> {
                    DefenseGroupResponse.TeacherInfo info = new DefenseGroupResponse.TeacherInfo();
                    info.setId(gt.getTeacherId());
                    info.setTeacherOrder(gt.getTeacherOrder());
                    userRepository.findById(gt.getTeacherId())
                            .ifPresent(u -> info.setName(u.getName()));
                    return info;
                })
                .collect(Collectors.toList());

        // 统计学生数量
        int studentCount = (int) studentRepository.countByGroupId(group.getId());

        return DefenseGroupResponse.fromEntity(group, deptName, yearName, leaderName, teachers, studentCount);
    }
}
