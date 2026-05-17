package com.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.admin.common.BusinessException;
import com.admin.common.PageResult;
import com.admin.dto.RoleSaveRequest;
import com.admin.entity.SysRole;
import com.admin.mapper.SysPermissionMapper;
import com.admin.mapper.SysRoleMapper;
import com.admin.mapper.SysRolePermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService extends ServiceImpl<SysRoleMapper, SysRole> {

    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysPermissionMapper permissionMapper;

    public PageResult<SysRole> page(int current, int size, String keyword) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysRole::getRoleCode, keyword)
                    .or().like(SysRole::getRoleName, keyword));
        }
        wrapper.orderByDesc(SysRole::getCreatedAt);
        Page<SysRole> page = page(new Page<>(current, size), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    public List<SysRole> listAll() {
        return list(new LambdaQueryWrapper<SysRole>().eq(SysRole::getStatus, 1));
    }

    public SysRole detail(Long id) {
        SysRole role = getById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        role.setPermissionIds(permissionMapper.selectPermissionIdsByRoleId(id));
        return role;
    }

    @Transactional
    public void create(RoleSaveRequest req) {
        if (count(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, req.getRoleCode())) > 0) {
            throw new BusinessException("角色编码已存在");
        }
        SysRole role = new SysRole();
        role.setRoleCode(req.getRoleCode());
        role.setRoleName(req.getRoleName());
        role.setDescription(req.getDescription());
        role.setStatus(req.getStatus());
        save(role);
        savePermissions(role.getId(), req.getPermissionIds());
    }

    @Transactional
    public void update(RoleSaveRequest req) {
        SysRole role = getById(req.getId());
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        role.setRoleName(req.getRoleName());
        role.setDescription(req.getDescription());
        role.setStatus(req.getStatus());
        updateById(role);
        rolePermissionMapper.deleteByRoleId(role.getId());
        savePermissions(role.getId(), req.getPermissionIds());
    }

    public void delete(Long id) {
        if (id <= 3L) {
            throw new BusinessException("内置角色不可删除");
        }
        removeById(id);
        rolePermissionMapper.deleteByRoleId(id);
    }

    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        SysRole role = getById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        rolePermissionMapper.deleteByRoleId(roleId);
        savePermissions(roleId, permissionIds);
    }

    private void savePermissions(Long roleId, List<Long> permissionIds) {
        if (permissionIds != null && !permissionIds.isEmpty()) {
            rolePermissionMapper.insertBatch(roleId, permissionIds);
        }
    }
}
