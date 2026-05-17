package com.admin.controller;

import com.admin.common.R;
import com.admin.dto.PermissionIconRequest;
import com.admin.dto.PermissionSaveRequest;
import com.admin.entity.SysPermission;
import com.admin.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('sys:perm:list')")
    public R<List<SysPermission>> tree() {
        return R.ok(permissionService.listAllTree());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:perm:list')")
    public R<SysPermission> detail(@PathVariable Long id) {
        return R.ok(permissionService.detail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sys:perm:add')")
    public R<Void> create(@Validated @RequestBody PermissionSaveRequest request) {
        permissionService.create(request);
        return R.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:perm:edit')")
    public R<Void> update(@PathVariable Long id, @Validated @RequestBody PermissionSaveRequest request) {
        request.setId(id);
        permissionService.update(request);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:perm:delete')")
    public R<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return R.ok();
    }

    @PutMapping("/{id}/icon")
    @PreAuthorize("hasAuthority('sys:perm:edit')")
    public R<Void> updateIcon(@PathVariable Long id, @Validated @RequestBody PermissionIconRequest request) {
        permissionService.updateIcon(id, request.getIcon());
        return R.ok();
    }
}
