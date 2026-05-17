package com.admin.vo.monitor;

import lombok.Data;

import java.util.List;

@Data
public class CacheMonitorVO {
    private boolean redisEnabled;
    private String redisMessage;
    private int onlineSessionCount;
    private int tokenBlacklistCount;
    private int websocketConnectionCount;
    private List<MemoryItem> memoryRegions;

    @Data
    public static class MemoryItem {
        private String name;
        private long usedBytes;
        private long maxBytes;
        private double usagePercent;
    }
}
