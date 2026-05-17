package com.admin.vo.monitor;

import lombok.Data;

@Data
public class DataSourceMonitorVO {
    private String poolName;
    private String dbProduct;
    private String dbVersion;
    private String jdbcUrl;
    private String driverName;
    private int activeConnections;
    private int idleConnections;
    private int totalConnections;
    private int threadsAwaitingConnection;
    private int maxPoolSize;
    private int minIdle;
    private long connectionTimeoutMs;
}
