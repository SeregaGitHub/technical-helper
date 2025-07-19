package ru.kraser.technical_helper.main_server.security;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@UtilityClass
public class SecurityUtil {

    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();

        return userDetails.getUserId();
    }
}
