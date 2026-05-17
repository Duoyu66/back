package com.admin.vo.monitor;

import lombok.Data;

@Data
public class JobMonitorVO {
    private String jobId;
    private String jobName;
    private String cron;
    private String status;
    private String description;
    private String lastRunTime;
    private String nextRunHint;
}
