package com.admin.controller;

import com.admin.common.PageResult;
import com.admin.common.R;
import com.admin.entity.SysLoginLog;
import com.admin.entity.SysOperLog;
import com.admin.service.LoginLogService;
import com.admin.service.OperLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final OperLogService operLogService;
    private final LoginLogService loginLogService;

    @GetMapping("/oper")
    @PreAuthorize("hasAuthority('sys:log:oper')")
    public R<PageResult<SysOperLog>> operPage(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return R.ok(operLogService.page(current, size, keyword, status));
    }

    @GetMapping("/login")
    @PreAuthorize("hasAuthority('sys:log:login')")
    public R<PageResult<SysLoginLog>> loginPage(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return R.ok(loginLogService.page(current, size, keyword, status));
    }
}
