package com.admin.aspect;

import com.admin.annotation.OperLog;
import com.admin.entity.SysOperLog;
import com.admin.security.LoginUser;
import com.admin.service.OperLogService;
import com.admin.util.HttpUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    private static final Set<String> SKIP_CONTROLLERS = Set.of(
            "AuthController", "LogController", "DashboardController", "MonitorController"
    );

    private static final Map<String, String> MODULE_NAMES = Map.ofEntries(
            Map.entry("UserController", "用户管理"),
            Map.entry("RoleController", "角色管理"),
            Map.entry("PermissionController", "权限配置"),
            Map.entry("DeptController", "部门管理"),
            Map.entry("NoticeController", "公告管理")
    );

    private final OperLogService operLogService;
    private final ObjectMapper objectMapper;

    @Around("execution(* com.admin.controller..*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        if (shouldSkip(pjp)) {
            return pjp.proceed();
        }

        long start = System.currentTimeMillis();
        SysOperLog log = new SysOperLog();
        fillRequest(log);
        fillUser(log);
        fillMeta(pjp, log);

        try {
            Object result = pjp.proceed();
            log.setStatus(1);
            return result;
        } catch (Throwable ex) {
            log.setStatus(0);
            log.setErrorMsg(truncate(ex.getMessage(), 500));
            throw ex;
        } finally {
            log.setCostMs(System.currentTimeMillis() - start);
            operLogService.saveAsync(log);
        }
    }

    private boolean shouldSkip(ProceedingJoinPoint pjp) {
        String className = pjp.getTarget().getClass().getSimpleName();
        if (SKIP_CONTROLLERS.contains(className)) {
            return true;
        }
        HttpServletRequest request = HttpUtils.currentRequest();
        if (request == null || "GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String methodName = pjp.getSignature().getName();
        if ("NoticeController".equals(className)) {
            return Set.of("inbox", "unreadCount", "markRead", "markAllRead").contains(methodName);
        }
        return false;
    }

    private void fillMeta(ProceedingJoinPoint pjp, SysOperLog log) {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        OperLog ann = method.getAnnotation(OperLog.class);
        String className = pjp.getTarget().getClass().getSimpleName();
        if (ann != null && !ann.module().isBlank()) {
            log.setModule(ann.module());
        } else {
            log.setModule(MODULE_NAMES.getOrDefault(className, className));
        }
        if (ann != null && !ann.operation().isBlank()) {
            log.setOperation(ann.operation());
        } else {
            log.setOperation(method.getName());
        }
        log.setMethod(pjp.getSignature().toShortString());
        log.setParams(truncate(safeParams(pjp), 2000));
    }

    private void fillUser(SysOperLog log) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser loginUser) {
            log.setUserId(loginUser.getSysUser().getId());
            log.setUsername(loginUser.getUsername());
        }
    }

    private void fillRequest(SysOperLog log) {
        HttpServletRequest request = HttpUtils.currentRequest();
        if (request == null) {
            return;
        }
        log.setRequestUri(request.getRequestURI());
        log.setRequestMethod(request.getMethod());
        log.setIp(HttpUtils.getClientIp(request));
        log.setUserAgent(truncate(HttpUtils.getUserAgent(request), 500));
    }

    private String safeParams(ProceedingJoinPoint pjp) {
        try {
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            String[] names = signature.getParameterNames();
            Object[] args = pjp.getArgs();
            if (names == null || args == null) {
                return null;
            }
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < names.length; i++) {
                Object arg = args[i];
                if (arg instanceof HttpServletRequest) {
                    continue;
                }
                String key = names[i];
                if (key != null && key.toLowerCase().contains("password")) {
                    map.put(key, "******");
                } else {
                    map.put(key, arg);
                }
            }
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return null;
        }
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() > max ? s.substring(0, max) : s;
    }
}
