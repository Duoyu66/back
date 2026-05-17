package com.admin.service;

import com.admin.common.BusinessException;
import com.admin.monitor.OnlineUserRegistry;
import com.admin.monitor.SessionTokenBlacklist;
import com.admin.vo.OnlineUserVO;
import com.admin.vo.monitor.CacheMonitorVO;
import com.admin.vo.monitor.DataSourceMonitorVO;
import com.admin.vo.monitor.JobMonitorVO;
import com.admin.vo.monitor.ServerMonitorVO;
import com.admin.websocket.NoticeWebSocketSessionManager;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.RequiredArgsConstructor;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MonitorService {

    private final OnlineUserRegistry onlineUserRegistry;
    private final SessionTokenBlacklist sessionTokenBlacklist;
    private final NoticeWebSocketSessionManager webSocketSessionManager;
    private final DataSource dataSource;

    @Value("${spring.application.name:admin}")
    private String applicationName;

    public List<OnlineUserVO> listOnlineUsers(String username, String ip) {
        return onlineUserRegistry.list(username, ip);
    }

    public void forceLogout(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new BusinessException("会话不存在");
        }
        onlineUserRegistry.remove(sessionId);
        sessionTokenBlacklist.block(sessionId);
    }

    public ServerMonitorVO getServerInfo() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();

        ServerMonitorVO vo = new ServerMonitorVO();
        vo.setComputerName(os.getNetworkParams().getHostName());
        vo.setOsName(os.getFamily());
        vo.setOsArch(System.getProperty("os.arch"));
        vo.setOsVersion(os.getVersionInfo().getVersion());
        vo.setJavaVersion(System.getProperty("java.version"));
        vo.setJavaHome(System.getProperty("java.home"));
        vo.setProjectPath(System.getProperty("user.dir"));
        vo.setJvmUptimeMs(ManagementFactory.getRuntimeMXBean().getUptime());

        try {
            vo.setServerIp(InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            vo.setServerIp("127.0.0.1");
        }

        CentralProcessor processor = hal.getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        double cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        ServerMonitorVO.CpuInfo cpu = new ServerMonitorVO.CpuInfo();
        cpu.setCores(processor.getLogicalProcessorCount());
        cpu.setUsagePercent(Math.round(cpuLoad * 100.0) / 100.0);
        vo.setCpu(cpu);

        GlobalMemory memory = hal.getMemory();
        long memTotal = memory.getTotal();
        long memAvailable = memory.getAvailable();
        long memUsed = memTotal - memAvailable;
        ServerMonitorVO.MemoryInfo mem = new ServerMonitorVO.MemoryInfo();
        mem.setTotalBytes(memTotal);
        mem.setUsedBytes(memUsed);
        mem.setUsagePercent(memTotal > 0 ? Math.round(memUsed * 10000.0 / memTotal) / 100.0 : 0);
        vo.setMemory(mem);

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeap = memoryBean.getNonHeapMemoryUsage();
        ServerMonitorVO.JvmMemoryInfo jvm = new ServerMonitorVO.JvmMemoryInfo();
        jvm.setHeapUsed(heap.getUsed());
        jvm.setHeapMax(heap.getMax() > 0 ? heap.getMax() : heap.getCommitted());
        jvm.setNonHeapUsed(nonHeap.getUsed());
        jvm.setHeapUsagePercent(jvm.getHeapMax() > 0
                ? Math.round(heap.getUsed() * 10000.0 / jvm.getHeapMax()) / 100.0
                : 0);
        vo.setJvm(jvm);

        FileSystem fs = os.getFileSystem();
        List<ServerMonitorVO.DiskInfo> disks = new ArrayList<>();
        for (OSFileStore store : fs.getFileStores()) {
            long total = store.getTotalSpace();
            if (total <= 0) {
                continue;
            }
            long usable = store.getUsableSpace();
            long used = total - usable;
            ServerMonitorVO.DiskInfo disk = new ServerMonitorVO.DiskInfo();
            disk.setMount(store.getMount());
            disk.setType(store.getType());
            disk.setTotalBytes(total);
            disk.setUsedBytes(used);
            disk.setUsagePercent(Math.round(used * 10000.0 / total) / 100.0);
            disks.add(disk);
        }
        vo.setDisks(disks);
        return vo;
    }

    public DataSourceMonitorVO getDataSourceInfo() {
        if (!(dataSource instanceof HikariDataSource hikari)) {
            throw new BusinessException("当前数据源不是 HikariCP，无法采集连接池指标");
        }
        HikariPoolMXBean pool = hikari.getHikariPoolMXBean();
        DataSourceMonitorVO vo = new DataSourceMonitorVO();
        vo.setPoolName(hikari.getPoolName());
        vo.setJdbcUrl(hikari.getJdbcUrl());
        vo.setDriverName(hikari.getDriverClassName());
        vo.setMaxPoolSize(hikari.getMaximumPoolSize());
        vo.setMinIdle(hikari.getMinimumIdle());
        vo.setConnectionTimeoutMs(hikari.getConnectionTimeout());
        if (pool != null) {
            vo.setActiveConnections(pool.getActiveConnections());
            vo.setIdleConnections(pool.getIdleConnections());
            vo.setTotalConnections(pool.getTotalConnections());
            vo.setThreadsAwaitingConnection(pool.getThreadsAwaitingConnection());
        }
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            vo.setDbProduct(meta.getDatabaseProductName());
            vo.setDbVersion(meta.getDatabaseProductVersion());
        } catch (Exception e) {
            vo.setDbProduct("未知");
            vo.setDbVersion(e.getMessage());
        }
        return vo;
    }

    public CacheMonitorVO getCacheInfo() {
        CacheMonitorVO vo = new CacheMonitorVO();
        vo.setRedisEnabled(false);
        vo.setRedisMessage("未配置 Redis，当前为 JVM 内存与在线会话监控");
        vo.setOnlineSessionCount(onlineUserRegistry.onlineCount());
        vo.setTokenBlacklistCount(sessionTokenBlacklist.size());
        vo.setWebsocketConnectionCount(webSocketSessionManager.connectionCount());

        List<CacheMonitorVO.MemoryItem> regions = new ArrayList<>();
        MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
        addMemoryRegion(regions, "堆内存", bean.getHeapMemoryUsage());
        addMemoryRegion(regions, "非堆内存", bean.getNonHeapMemoryUsage());
        vo.setMemoryRegions(regions);
        return vo;
    }

    public List<JobMonitorVO> listJobs() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        List<JobMonitorVO> jobs = new ArrayList<>();

        JobMonitorVO cleanup = new JobMonitorVO();
        cleanup.setJobId("online-session-cleanup");
        cleanup.setJobName("在线会话清理");
        cleanup.setCron("每 5 分钟");
        cleanup.setStatus("运行中");
        cleanup.setDescription("清理超过 30 分钟无访问的在线会话记录");
        cleanup.setLastRunTime(now);
        cleanup.setNextRunHint("约 5 分钟后");
        jobs.add(cleanup);

        JobMonitorVO operLog = new JobMonitorVO();
        operLog.setJobId("oper-log-retention");
        operLog.setJobName("操作日志保留策略");
        operLog.setCron("手动/待接入");
        operLog.setStatus("已登记");
        operLog.setDescription("可在后续版本接入定时归档 sys_oper_log");
        operLog.setLastRunTime("-");
        operLog.setNextRunHint("未调度");
        jobs.add(operLog);

        return jobs;
    }

    private void addMemoryRegion(List<CacheMonitorVO.MemoryItem> regions, String name, MemoryUsage usage) {
        CacheMonitorVO.MemoryItem item = new CacheMonitorVO.MemoryItem();
        item.setName(name);
        item.setUsedBytes(usage.getUsed());
        long max = usage.getMax() > 0 ? usage.getMax() : usage.getCommitted();
        item.setMaxBytes(max);
        item.setUsagePercent(max > 0 ? Math.round(usage.getUsed() * 10000.0 / max) / 100.0 : 0);
        regions.add(item);
    }
}
