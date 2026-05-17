package com.admin.vo;

import lombok.Data;

import java.util.List;

@Data
public class DashboardStatsVO {
    private long userTotal;
    private long userEnabled;
    private long userDisabled;
    private long roleCount;
    private long deptCount;
    private long permissionCount;

    /** 近 7 日新增用户 */
    private List<NameValueVO> userTrend;
    /** 部门用户分布 */
    private List<NameValueVO> usersByDept;
    /** 角色用户分布 */
    private List<NameValueVO> usersByRole;
    /** 用户状态 */
    private List<NameValueVO> userStatus;

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
}
