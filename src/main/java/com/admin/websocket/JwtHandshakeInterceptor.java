package com.admin.websocket;

import com.admin.monitor.SessionTokenBlacklist;
import com.admin.security.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    public static final String ATTR_USER_ID = "userId";
    public static final String ATTR_SESSION_ID = "sessionId";

    private final JwtUtils jwtUtils;
    private final SessionTokenBlacklist sessionTokenBlacklist;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = resolveToken(request);
        if (!StringUtils.hasText(token) || !jwtUtils.validate(token)) {
            return false;
        }
        Claims claims = jwtUtils.parse(token);
        String sessionId = jwtUtils.getSessionId(claims);
        if (sessionTokenBlacklist.isBlocked(sessionId)) {
            return false;
        }
        Number userId = claims.get("userId", Number.class);
        if (userId == null) {
            return false;
        }
        attributes.put(ATTR_USER_ID, userId.longValue());
        attributes.put(ATTR_SESSION_ID, sessionId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }

    private String resolveToken(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter("token");
            if (StringUtils.hasText(token)) {
                return token;
            }
        }
        String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }
}
