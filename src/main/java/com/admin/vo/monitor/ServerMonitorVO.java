package com.admin.vo.monitor;

import lombok.Data;

import java.util.List;

@Data
public class ServerMonitorVO {
    private String computerName;
    private String osName;
    private String osArch;
    private String osVersion;
    private String serverIp;
    private String javaVersion;
    private String javaHome;
    private String projectPath;
    private long jvmUptimeMs;
    private CpuInfo cpu;
    private MemoryInfo memory;
    private JvmMemoryInfo jvm;
    private List<DiskInfo> disks;

    @Data
    public static class CpuInfo {
        private int cores;
        private double usagePercent;
    }

    @Data
    public static class MemoryInfo {
        private long totalBytes;
        private long usedBytes;
        private double usagePercent;
    }

    @Data
    public static class JvmMemoryInfo {
        private long heapUsed;
        private long heapMax;
        private long nonHeapUsed;
        private double heapUsagePercent;
    }

    @Data
    public static class DiskInfo {
        private String mount;
        private String type;
        private long totalBytes;
        private long usedBytes;
        private double usagePercent;
    }
}
