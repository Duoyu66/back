package com.admin.controller;

import com.admin.common.R;
import com.admin.dto.DeptSaveRequest;
import com.admin.entity.SysDept;
import com.admin.service.DeptService;
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
@RequestMapping("/api/depts")
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('sys:dept:list')")
    public R<List<SysDept>> tree() {
        return R.ok(deptService.listTree());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:dept:list')")
    public R<SysDept> detail(@PathVariable Long id) {
        return R.ok(deptService.detail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sys:dept:add')")
    public R<Void> create(@Validated @RequestBody DeptSaveRequest request) {
        deptService.create(request);
        return R.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:dept:edit')")
    public R<Void> update(@PathVariable Long id, @Validated @RequestBody DeptSaveRequest request) {
        request.setId(id);
        deptService.update(request);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:dept:delete')")
    public R<Void> delete(@PathVariable Long id) {
        deptService.delete(id);
        return R.ok();
    }
}
