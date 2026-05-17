package com.admin.controller;

import com.admin.common.R;
import com.admin.service.MonitorService;
import com.admin.vo.OnlineUserVO;
import com.admin.vo.monitor.CacheMonitorVO;
import com.admin.vo.monitor.DataSourceMonitorVO;
import com.admin.vo.monitor.JobMonitorVO;
import com.admin.vo.monitor.ServerMonitorVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final MonitorService monitorService;

    @GetMapping("/online")
    @PreAuthorize("hasAuthority('sys:monitor:online')")
    public R<List<OnlineUserVO>> online(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String ip) {
        return R.ok(monitorService.listOnlineUsers(username, ip));
    }

    @DeleteMapping("/online/{sessionId}")
    @PreAuthorize("hasAuthority('sys:monitor:kick')")
    public R<Void> forceLogout(@PathVariable String sessionId) {
        monitorService.forceLogout(sessionId);
        return R.ok();
    }

    @GetMapping("/server")
    @PreAuthorize("hasAuthority('sys:monitor:server')")
    public R<ServerMonitorVO> server() {
        return R.ok(monitorService.getServerInfo());
    }

    @GetMapping("/datasource")
    @PreAuthorize("hasAuthority('sys:monitor:datasource')")
    public R<DataSourceMonitorVO> datasource() {
        return R.ok(monitorService.getDataSourceInfo());
    }

    @GetMapping("/cache")
    @PreAuthorize("hasAuthority('sys:monitor:cache')")
    public R<CacheMonitorVO> cache() {
        return R.ok(monitorService.getCacheInfo());
    }

    @GetMapping("/jobs")
    @PreAuthorize("hasAuthority('sys:monitor:job')")
    public R<List<JobMonitorVO>> jobs() {
        return R.ok(monitorService.listJobs());
    }
}
