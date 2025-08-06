package ru.kraser.technical_helper.common_module.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.kraser.technical_helper.common_module.dto.user.UserIdsDto;
import ru.kraser.technical_helper.common_module.model.JwtUserDetails;

@UtilityClass
public class SecurityUtil {

    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();

        return userDetails.getUserId();
    }

    public UserIdsDto getUserIds() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();

        return UserIdsDto.builder()
                .userId(userDetails.getUserId())
                .departmentId(userDetails.getUserDepartmentId())
                .build();
    }
}
