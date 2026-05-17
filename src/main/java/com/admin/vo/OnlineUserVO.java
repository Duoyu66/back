package com.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OnlineUserVO {
    private String sessionId;
    private Long userId;
    private String username;
    private String nickname;
    private String deptName;
    private String ip;
    private String loginLocation;
    private String browser;
    private String os;
    private LocalDateTime loginTime;
    private LocalDateTime lastAccessTime;
}
