package ru.kraser.technical_helper.common_module.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import ru.kraser.technical_helper.common_module.enums.Role;

import java.time.LocalDateTime;

@Builder
public record UserFullDto(
        String id,
        String username,
        String password,
        boolean enabled,
        Role role,
        String createdBy,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdDate,
        String lastUpdatedBy,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime lastUpdatedDate,
        String departmentId,
        String departmentName,
        boolean departmentEnabled,
        String departmentCreatedBy,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime departmentCreatedDate,
        String departmentLastUpdatedBy,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime departmentLastUpdatedDate
) {
}
