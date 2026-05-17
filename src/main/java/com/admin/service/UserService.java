package com.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.admin.common.BusinessException;
import com.admin.common.PageResult;
import com.admin.dto.UserSaveRequest;
import com.admin.entity.SysRole;
import com.admin.entity.SysUser;
import com.admin.entity.SysDept;
import com.admin.mapper.SysDeptMapper;
import com.admin.mapper.SysRoleMapper;
import com.admin.mapper.SysUserMapper;
import com.admin.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<SysUserMapper, SysUser> {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysDeptMapper deptMapper;
    private final PasswordEncoder passwordEncoder;

    public PageResult<SysUser> page(int current, int size, String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getNickname, keyword)
                    .or().like(SysUser::getEmail, keyword));
        }
        wrapper.orderByDesc(SysUser::getCreatedAt);
        Page<SysUser> page = page(new Page<>(current, size), wrapper);
        page.getRecords().forEach(u -> {
            fillRoles(u);
            fillDept(u);
        });
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    public SysUser detail(Long id) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);
        fillRoles(user);
        fillDept(user);
        user.setRoleIds(userRoleMapper.selectRoleIdsByUserId(id));
        return user;
    }

    @Transactional
    public void create(UserSaveRequest req) {
        if (count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, req.getUsername())) > 0) {
            throw new BusinessException("用户名已存在");
        }
        SysUser user = new SysUser();
        copy(req, user);
        user.setPassword(passwordEncoder.encode(
                StringUtils.hasText(req.getPassword()) ? req.getPassword() : "admin123"));
        if (user.getAvatar() == null && StringUtils.hasText(user.getNickname())) {
            user.setAvatar(user.getNickname().substring(0, 1).toUpperCase());
        }
        save(user);
        saveUserRoles(user.getId(), req.getRoleIds());
    }

    @Transactional
    public void update(UserSaveRequest req) {
        SysUser user = getById(req.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setNickname(req.getNickname());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setDeptId(req.getDeptId());
        user.setStatus(req.getStatus());
        if (StringUtils.hasText(req.getPassword())) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        updateById(user);
        userRoleMapper.deleteByUserId(user.getId());
        saveUserRoles(user.getId(), req.getRoleIds());
    }

    public void delete(Long id) {
        if (id == 1L) {
            throw new BusinessException("不能删除超级管理员");
        }
        removeById(id);
        userRoleMapper.deleteByUserId(id);
    }

    public void resetPassword(Long id, String newPassword) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(
                StringUtils.hasText(newPassword) ? newPassword : "admin123"));
        updateById(user);
    }

    private void copy(UserSaveRequest req, SysUser user) {
        user.setUsername(req.getUsername());
        user.setNickname(req.getNickname());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setDeptId(req.getDeptId());
        user.setStatus(req.getStatus());
    }

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds != null && !roleIds.isEmpty()) {
            userRoleMapper.insertBatch(userId, roleIds);
        }
    }

    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        userRoleMapper.deleteByUserId(userId);
        saveUserRoles(userId, roleIds);
    }

    private void fillDept(SysUser user) {
        if (user.getDeptId() == null) {
            return;
        }
        SysDept dept = deptMapper.selectById(user.getDeptId());
        if (dept != null) {
            user.setDeptName(dept.getDeptName());
        }
    }

    private void fillRoles(SysUser user) {
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(user.getId());
        user.setRoleIds(roleIds);
        if (!roleIds.isEmpty()) {
            List<SysRole> roles = roleMapper.selectBatchIds(roleIds);
            user.setRoleNames(roles.stream().map(SysRole::getRoleName).collect(Collectors.toList()));
        }
    }
}
