package com.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeInboxVO {
    private Long id;
    private String title;
    private String content;
    private Integer noticeType;
    private LocalDateTime publishTime;
    private Boolean read;
}
