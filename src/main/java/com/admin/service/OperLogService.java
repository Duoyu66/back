package com.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.admin.common.PageResult;
import com.admin.entity.SysOperLog;
import com.admin.mapper.SysOperLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class OperLogService {

    private final SysOperLogMapper operLogMapper;

    public PageResult<SysOperLog> page(int current, int size, String keyword, Integer status) {
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysOperLog::getUsername, keyword)
                    .or().like(SysOperLog::getModule, keyword)
                    .or().like(SysOperLog::getOperation, keyword));
        }
        if (status != null) {
            wrapper.eq(SysOperLog::getStatus, status);
        }
        wrapper.orderByDesc(SysOperLog::getCreatedAt);
        Page<SysOperLog> page = operLogMapper.selectPage(new Page<>(current, size), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Async
    public void saveAsync(SysOperLog log) {
        operLogMapper.insert(log);
    }
}
