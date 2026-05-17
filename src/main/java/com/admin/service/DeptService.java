package com.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.admin.common.BusinessException;
import com.admin.dto.DeptSaveRequest;
import com.admin.entity.SysDept;
import com.admin.mapper.SysDeptMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeptService {

    private final SysDeptMapper deptMapper;

    public List<SysDept> listTree() {
        List<SysDept> all = deptMapper.selectList(
                new LambdaQueryWrapper<SysDept>().orderByAsc(SysDept::getSortOrder));
        return buildTree(all, 0L);
    }

    public SysDept detail(Long id) {
        SysDept dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }
        return dept;
    }

    public void create(DeptSaveRequest req) {
        validateParent(req.getParentId(), null);
        if (deptMapper.selectCount(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getDeptCode, req.getDeptCode())) > 0) {
            throw new BusinessException("部门编码已存在");
        }
        SysDept dept = toEntity(req);
        deptMapper.insert(dept);
    }

    public void update(DeptSaveRequest req) {
        SysDept dept = deptMapper.selectById(req.getId());
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }
        if (req.getId().equals(req.getParentId())) {
            throw new BusinessException("上级部门不能是自己");
        }
        validateParent(req.getParentId(), req.getId());
        if (deptMapper.selectCount(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getDeptCode, req.getDeptCode())
                .ne(SysDept::getId, req.getId())) > 0) {
            throw new BusinessException("部门编码已存在");
        }
        apply(req, dept);
        deptMapper.updateById(dept);
    }

    public void delete(Long id) {
        if (id == 1L) {
            throw new BusinessException("根部门不可删除");
        }
        SysDept dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }
        long childCount = deptMapper.selectCount(
                new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("存在子部门，无法删除");
        }
        if (deptMapper.countUsersByDeptId(id) > 0) {
            throw new BusinessException("部门下仍有用户，无法删除");
        }
        deptMapper.deleteById(id);
    }

    private void validateParent(Long parentId, Long selfId) {
        if (parentId == null || parentId == 0L) {
            return;
        }
        SysDept parent = deptMapper.selectById(parentId);
        if (parent == null) {
            throw new BusinessException("上级部门不存在");
        }
        if (selfId != null && isDescendant(parentId, selfId)) {
            throw new BusinessException("上级部门不能是当前部门的子级");
        }
    }

    private boolean isDescendant(Long parentId, Long selfId) {
        if (parentId.equals(selfId)) {
            return true;
        }
        SysDept parent = deptMapper.selectById(parentId);
        if (parent == null || parent.getParentId() == null || parent.getParentId() == 0L) {
            return false;
        }
        return isDescendant(parent.getParentId(), selfId);
    }

    private SysDept toEntity(DeptSaveRequest req) {
        SysDept dept = new SysDept();
        apply(req, dept);
        return dept;
    }

    private void apply(DeptSaveRequest req, SysDept dept) {
        dept.setParentId(req.getParentId() != null ? req.getParentId() : 0L);
        dept.setDeptName(req.getDeptName());
        dept.setDeptCode(req.getDeptCode());
        dept.setLeader(StringUtils.hasText(req.getLeader()) ? req.getLeader() : null);
        dept.setPhone(StringUtils.hasText(req.getPhone()) ? req.getPhone() : null);
        dept.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
        dept.setStatus(req.getStatus());
    }

    private List<SysDept> buildTree(List<SysDept> all, Long parentId) {
        List<SysDept> result = new ArrayList<>();
        for (SysDept dept : all) {
            if (parentId.equals(dept.getParentId())) {
                List<SysDept> kids = buildTree(all, dept.getId());
                dept.setChildren(kids.isEmpty() ? null : kids);
                result.add(dept);
            }
        }
        return result;
    }
}
