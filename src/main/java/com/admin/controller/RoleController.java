package com.admin.controller;

import com.admin.common.PageResult;
import com.admin.common.R;
import com.admin.dto.RoleSaveRequest;
import com.admin.entity.SysRole;
import com.admin.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('sys:role:list')")
    public R<PageResult<SysRole>> page(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return R.ok(roleService.page(current, size, keyword));
    }

    @GetMapping("/all")
    public R<List<SysRole>> listAll() {
        return R.ok(roleService.listAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:role:list')")
    public R<SysRole> detail(@PathVariable Long id) {
        return R.ok(roleService.detail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sys:role:add')")
    public R<Void> create(@Validated @RequestBody RoleSaveRequest request) {
        roleService.create(request);
        return R.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:role:edit')")
    public R<Void> update(@PathVariable Long id, @Validated @RequestBody RoleSaveRequest request) {
        request.setId(id);
        roleService.update(request);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:role:delete')")
    public R<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return R.ok();
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('sys:role:perm')")
    public R<Void> assignPermissions(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        roleService.assignPermissions(id, body.get("permissionIds"));
        return R.ok();
    }
}
