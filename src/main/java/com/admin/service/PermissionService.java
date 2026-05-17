package com.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.admin.common.BusinessException;
import com.admin.dto.PermissionSaveRequest;
import com.admin.entity.SysPermission;
import com.admin.mapper.SysPermissionMapper;
import com.admin.mapper.SysRolePermissionMapper;
import com.admin.vo.MenuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final SysPermissionMapper permissionMapper;
    private final SysRolePermissionMapper rolePermissionMapper;

    public List<MenuVO> getMenuTreeByUserId(Long userId) {
        List<SysPermission> menus = permissionMapper.selectMenusByUserId(userId);
        return buildMenuTree(menus);
    }

    public List<SysPermission> listAllTree() {
        List<SysPermission> all = permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>().orderByAsc(SysPermission::getSortOrder));
        return buildPermTree(all, 0L);
    }

    public SysPermission detail(Long id) {
        SysPermission perm = permissionMapper.selectById(id);
        if (perm == null) {
            throw new BusinessException("权限不存在");
        }
        return perm;
    }

    public void create(PermissionSaveRequest req) {
        validateRequest(req, null);
        if (permissionMapper.selectCount(new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getPermCode, req.getPermCode())) > 0) {
            throw new BusinessException("权限标识已存在");
        }
        SysPermission perm = toEntity(req);
        permissionMapper.insert(perm);
    }

    public void update(PermissionSaveRequest req) {
        SysPermission perm = permissionMapper.selectById(req.getId());
        if (perm == null) {
            throw new BusinessException("权限不存在");
        }
        validateRequest(req, req.getId());
        if (permissionMapper.selectCount(new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getPermCode, req.getPermCode())
                .ne(SysPermission::getId, req.getId())) > 0) {
            throw new BusinessException("权限标识已存在");
        }
        apply(req, perm);
        permissionMapper.updateById(perm);
    }

    public void delete(Long id) {
        SysPermission perm = permissionMapper.selectById(id);
        if (perm == null) {
            throw new BusinessException("权限不存在");
        }
        long childCount = permissionMapper.selectCount(
                new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("存在子节点，无法删除");
        }
        if (rolePermissionMapper.countByPermissionId(id) > 0) {
            throw new BusinessException("权限已被角色引用，请先在角色管理中取消分配");
        }
        rolePermissionMapper.deleteByPermissionId(id);
        permissionMapper.deleteById(id);
    }

    public void updateIcon(Long id, String icon) {
        SysPermission perm = permissionMapper.selectById(id);
        if (perm == null) {
            throw new BusinessException("权限不存在");
        }
        if (perm.getPermType() == 2) {
            throw new BusinessException("按钮权限不支持设置图标");
        }
        perm.setIcon(icon);
        permissionMapper.updateById(perm);
    }

    private void validateRequest(PermissionSaveRequest req, Long selfId) {
        Long parentId = req.getParentId() != null ? req.getParentId() : 0L;
        if (selfId != null && selfId.equals(parentId)) {
            throw new BusinessException("上级节点不能是自己");
        }
        if (parentId > 0) {
            SysPermission parent = permissionMapper.selectById(parentId);
            if (parent == null) {
                throw new BusinessException("上级节点不存在");
            }
            if (parent.getPermType() == 2) {
                throw new BusinessException("按钮下不能再添加子节点");
            }
            if (selfId != null && isDescendant(parentId, selfId)) {
                throw new BusinessException("上级节点不能是当前节点的子级");
            }
        }
        int type = req.getPermType();
        if (type < 0 || type > 2) {
            throw new BusinessException("权限类型无效");
        }
        if (type == 1 && !StringUtils.hasText(req.getPath())) {
            throw new BusinessException("菜单类型必须填写路由路径");
        }
        if (type == 2 && parentId == 0) {
            throw new BusinessException("按钮必须挂在目录或菜单下");
        }
    }

    private boolean isDescendant(Long parentId, Long selfId) {
        if (parentId.equals(selfId)) {
            return true;
        }
        SysPermission parent = permissionMapper.selectById(parentId);
        if (parent == null || parent.getParentId() == null || parent.getParentId() == 0L) {
            return false;
        }
        return isDescendant(parent.getParentId(), selfId);
    }

    private SysPermission toEntity(PermissionSaveRequest req) {
        SysPermission perm = new SysPermission();
        apply(req, perm);
        return perm;
    }

    private void apply(PermissionSaveRequest req, SysPermission perm) {
        perm.setParentId(req.getParentId() != null ? req.getParentId() : 0L);
        perm.setPermCode(req.getPermCode());
        perm.setPermName(req.getPermName());
        perm.setPermType(req.getPermType());
        perm.setPath(StringUtils.hasText(req.getPath()) ? req.getPath() : null);
        perm.setIcon(StringUtils.hasText(req.getIcon()) ? req.getIcon() : null);
        perm.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
        perm.setStatus(req.getStatus());
    }

    private List<MenuVO> buildMenuTree(List<SysPermission> menus) {
        Map<Long, List<SysPermission>> grouped = menus.stream()
                .collect(Collectors.groupingBy(SysPermission::getParentId));
        return buildMenuChildren(grouped, 0L);
    }

    private List<MenuVO> buildMenuChildren(Map<Long, List<SysPermission>> grouped, Long parentId) {
        List<SysPermission> children = grouped.getOrDefault(parentId, new ArrayList<>());
        List<MenuVO> result = new ArrayList<>();
        for (SysPermission p : children) {
            MenuVO vo = toMenuVO(p);
            vo.setChildren(buildMenuChildren(grouped, p.getId()));
            if (vo.getChildren().isEmpty()) {
                vo.setChildren(null);
            }
            result.add(vo);
        }
        return result;
    }

    private List<SysPermission> buildPermTree(List<SysPermission> all, Long parentId) {
        List<SysPermission> result = new ArrayList<>();
        for (SysPermission p : all) {
            if (parentId.equals(p.getParentId())) {
                List<SysPermission> kids = buildPermTree(all, p.getId());
                p.setChildren(kids.isEmpty() ? null : kids);
                result.add(p);
            }
        }
        return result;
    }

    private MenuVO toMenuVO(SysPermission p) {
        MenuVO vo = new MenuVO();
        vo.setId(p.getId());
        vo.setParentId(p.getParentId());
        vo.setPermCode(p.getPermCode());
        vo.setPermName(p.getPermName());
        vo.setPermType(p.getPermType());
        vo.setPath(p.getPath());
        vo.setIcon(p.getIcon());
        vo.setSortOrder(p.getSortOrder());
        return vo;
    }
}
