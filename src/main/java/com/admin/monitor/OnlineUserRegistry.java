package com.admin.monitor;

import com.admin.entity.SysDept;
import com.admin.entity.SysUser;
import com.admin.mapper.SysDeptMapper;
import com.admin.security.LoginUser;
import com.admin.util.HttpUtils;
import com.admin.vo.OnlineUserVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OnlineUserRegistry {

    private final ConcurrentHashMap<String, OnlineUserVO> sessions = new ConcurrentHashMap<>();
    private final SysDeptMapper deptMapper;

    public void register(String sessionId, LoginUser loginUser, HttpServletRequest request) {
        SysUser user = loginUser.getSysUser();
        OnlineUserVO vo = buildVo(sessionId, user, request);
        vo.setLoginTime(LocalDateTime.now());
        vo.setLastAccessTime(vo.getLoginTime());
        sessions.put(sessionId, vo);
    }

    public void touch(String sessionId, LoginUser loginUser, HttpServletRequest request) {
        if (!StringUtils.hasText(sessionId)) {
            return;
        }
        sessions.compute(sessionId, (sid, existing) -> {
            OnlineUserVO vo = existing != null ? existing : buildVo(sid, loginUser.getSysUser(), request);
            if (existing == null) {
                vo.setLoginTime(LocalDateTime.now());
            }
            vo.setLastAccessTime(LocalDateTime.now());
            if (request != null) {
                vo.setIp(HttpUtils.getClientIp(request));
                parseUserAgent(HttpUtils.getUserAgent(request), vo);
            }
            return vo;
        });
    }

    public void remove(String sessionId) {
        if (StringUtils.hasText(sessionId)) {
            sessions.remove(sessionId);
        }
    }

    public List<OnlineUserVO> list(String username, String ip) {
        return sessions.values().stream()
                .filter(vo -> !StringUtils.hasText(username)
                        || (vo.getUsername() != null && vo.getUsername().contains(username)))
                .filter(vo -> !StringUtils.hasText(ip)
                        || (vo.getIp() != null && vo.getIp().contains(ip)))
                .sorted(Comparator.comparing(OnlineUserVO::getLastAccessTime).reversed())
                .collect(Collectors.toList());
    }

    public int cleanupIdle(long idleMinutes) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(idleMinutes);
        List<String> expired = sessions.entrySet().stream()
                .filter(e -> e.getValue().getLastAccessTime() != null
                        && e.getValue().getLastAccessTime().isBefore(threshold))
                .map(java.util.Map.Entry::getKey)
                .toList();
        expired.forEach(sessions::remove);
        return expired.size();
    }

    public int onlineCount() {
        return sessions.size();
    }

    private OnlineUserVO buildVo(String sessionId, SysUser user, HttpServletRequest request) {
        OnlineUserVO vo = new OnlineUserVO();
        vo.setSessionId(sessionId);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        if (user.getDeptId() != null) {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getDeptName());
            }
        }
        if (request != null) {
            vo.setIp(HttpUtils.getClientIp(request));
            parseUserAgent(HttpUtils.getUserAgent(request), vo);
        }
        vo.setLoginLocation(resolveLocation(vo.getIp()));
        return vo;
    }

    private void parseUserAgent(String ua, OnlineUserVO vo) {
        if (!StringUtils.hasText(ua)) {
            return;
        }
        String lower = ua.toLowerCase();
        vo.setBrowser(guessBrowser(lower));
        vo.setOs(guessOs(lower));
    }

    private String guessBrowser(String ua) {
        if (ua.contains("edg/")) return "Edge";
        if (ua.contains("chrome/")) return "Chrome";
        if (ua.contains("firefox/")) return "Firefox";
        if (ua.contains("safari/") && !ua.contains("chrome")) return "Safari";
        return "Unknown";
    }

    private String guessOs(String ua) {
        if (ua.contains("windows")) return "Windows";
        if (ua.contains("mac os")) return "Mac OS";
        if (ua.contains("linux")) return "Linux";
        if (ua.contains("android")) return "Android";
        if (ua.contains("iphone") || ua.contains("ipad")) return "iOS";
        return "Unknown";
    }

    private String resolveLocation(String ip) {
        if (!StringUtils.hasText(ip)) {
            return "未知";
        }
        if (ip.startsWith("127.") || "0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "本机";
        }
        if (ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.")) {
            return "内网";
        }
        return "外网";
    }
}
