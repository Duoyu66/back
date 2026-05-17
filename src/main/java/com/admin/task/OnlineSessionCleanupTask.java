package com.admin.task;

import com.admin.monitor.OnlineUserRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OnlineSessionCleanupTask {

    private final OnlineUserRegistry onlineUserRegistry;

    @Scheduled(cron = "0 */5 * * * ?")
    public void cleanupIdleSessions() {
        int removed = onlineUserRegistry.cleanupIdle(30);
        if (removed > 0) {
            log.debug("清理空闲在线会话 {} 条", removed);
        }
    }
}
