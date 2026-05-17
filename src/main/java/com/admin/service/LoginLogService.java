package com.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.admin.common.PageResult;
import com.admin.entity.SysLoginLog;
import com.admin.entity.SysUser;
import com.admin.mapper.SysLoginLogMapper;
import com.admin.mapper.SysUserMapper;
import com.admin.util.HttpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LoginLogService {

    private final SysLoginLogMapper loginLogMapper;
    private final SysUserMapper userMapper;

    public PageResult<SysLoginLog> page(int current, int size, String keyword, Integer status) {
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysLoginLog::getUsername, keyword);
        }
        if (status != null) {
            wrapper.eq(SysLoginLog::getStatus, status);
        }
        wrapper.orderByDesc(SysLoginLog::getCreatedAt);
        Page<SysLoginLog> page = loginLogMapper.selectPage(new Page<>(current, size), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Async
    public void record(String username, boolean success, String msg, HttpServletRequest request) {
        SysLoginLog log = new SysLoginLog();
        log.setUsername(username);
        log.setStatus(success ? 1 : 0);
        log.setMsg(msg);
        if (request != null) {
            log.setIp(HttpUtils.getClientIp(request));
            log.setUserAgent(HttpUtils.getUserAgent(request));
        }
        if (success) {
            SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUsername, username).last("LIMIT 1"));
            if (user != null) {
                log.setUserId(user.getId());
            }
        }
        loginLogMapper.insert(log);
    }
}
