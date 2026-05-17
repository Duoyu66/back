package com.admin.service;

import com.admin.entity.SysNotice;
import com.admin.vo.NoticeInboxVO;
import com.admin.websocket.NoticeWebSocketSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticePushService {

    private final NoticeWebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    public void pushPublished(SysNotice notice) {
        if (notice == null || notice.getId() == null) {
            return;
        }
        NoticeInboxVO data = new NoticeInboxVO();
        data.setId(notice.getId());
        data.setTitle(notice.getTitle());
        data.setContent(notice.getContent());
        data.setNoticeType(notice.getNoticeType());
        data.setPublishTime(notice.getPublishTime());
        data.setRead(false);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "NOTICE_PUBLISHED");
        payload.put("data", data);

        try {
            sessionManager.broadcast(objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            log.warn("公告 WebSocket 推送失败: {}", e.getMessage());
        }
    }
}
