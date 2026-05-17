package com.admin.vo;

import lombok.Data;

@Data
public class UserPreferencesVO {
    /** light | dark */
    private String colorMode;
    /** 主题预设 id，如 green、blue、violet */
    private String preset;
    /** 圆角系数 0 ~ 1 */
    private Double borderRadius;
}
