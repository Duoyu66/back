package com.admin.controller;

import com.admin.common.PageResult;
import com.admin.common.R;
import com.admin.dto.UserRoleAssignRequest;
import com.admin.dto.UserSaveRequest;
import com.admin.entity.SysUser;
import com.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('sys:user:list')")
    public R<PageResult<SysUser>> page(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long deptId) {
        return R.ok(userService.page(current, size, keyword, deptId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:user:list')")
    public R<SysUser> detail(@PathVariable Long id) {
        return R.ok(userService.detail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sys:user:add')")
    public R<Void> create(@Validated @RequestBody UserSaveRequest request) {
        userService.create(request);
        return R.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:user:edit')")
    public R<Void> update(@PathVariable Long id, @Validated @RequestBody UserSaveRequest request) {
        request.setId(id);
        userService.update(request);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:user:delete')")
    public R<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return R.ok();
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('sys:user:edit')")
    public R<Void> assignRoles(@PathVariable Long id, @Validated @RequestBody UserRoleAssignRequest request) {
        userService.assignRoles(id, request.getRoleIds());
        return R.ok();
    }

    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('sys:user:reset')")
    public R<Void> resetPassword(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String pwd = body != null ? body.get("password") : null;
        userService.resetPassword(id, pwd);
        return R.ok();
    }
}
