package com.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DashboardStatsVO {
    private long userTotal;
    private long userEnabled;
    private long userDisabled;
    private long roleCount;
    private long deptCount;
    private long permissionCount;
    private long noticeTotal;
    private long noticePublished;
    private long operLogToday;
    private long loginLogToday;

    /** 近 7 日新增用户 */
    private List<NameValueVO> userTrend;
    /** 部门用户分布 */
    private List<NameValueVO> usersByDept;
    /** 角色用户分布 */
    private List<NameValueVO> usersByRole;
    /** 用户状态 */
    private List<NameValueVO> userStatus;
    /** 最新公告 */
    private List<RecentNoticeVO> recentNotices;
    /** 最近操作日志 */
    private List<RecentOperLogVO> recentOperLogs;
    /** 最近登录日志 */
    private List<RecentLoginLogVO> recentLoginLogs;

    @Data
    public static class NameValueVO {
        private String name;
        private Long value;

        public NameValueVO() {}

        public NameValueVO(String name, Long value) {
            this.name = name;
            this.value = value;
        }
    }

    @Data
    public static class RecentNoticeVO {
        private Long id;
        private String title;
        private Integer noticeType;
        private LocalDateTime publishTime;
    }

    @Data
    public static class RecentOperLogVO {
        private String username;
        private String module;
        private String operation;
        private Integer status;
        private LocalDateTime createdAt;
    }

    @Data
    public static class RecentLoginLogVO {
        private String username;
        private String ip;
        private Integer status;
        private String msg;
        private LocalDateTime createdAt;
    }
}
