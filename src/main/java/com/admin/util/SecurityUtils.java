package com.admin.util;

import com.admin.common.BusinessException;
import com.admin.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static LoginUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUser)) {
            throw new BusinessException(401, "未登录");
        }
        return (LoginUser) auth.getPrincipal();
    }

    public static Long currentUserId() {
        return currentUser().getSysUser().getId();
    }
}
