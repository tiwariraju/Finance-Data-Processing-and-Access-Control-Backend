package com.finance.backend.util;

import com.finance.backend.entity.User;
import com.finance.backend.security.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SecurityUser su)) {
            throw new IllegalStateException("Authenticated user not available");
        }
        return su.getUser();
    }
}
