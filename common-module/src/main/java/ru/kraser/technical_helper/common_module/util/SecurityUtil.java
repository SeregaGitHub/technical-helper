package ru.kraser.technical_helper.common_module.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.common_module.model.Department;
import ru.kraser.technical_helper.common_module.model.JwtUserDetails;
import ru.kraser.technical_helper.common_module.model.User;

@UtilityClass
public class SecurityUtil {

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();

        return userDetails.getUser();
    }

    public String getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }

    public Department getCurrentUserDepartment() {
        return getCurrentUser().getDepartment();
    }

    public Role getCurrentUserRole() {
        return getCurrentUser().getRole();
    }
}
