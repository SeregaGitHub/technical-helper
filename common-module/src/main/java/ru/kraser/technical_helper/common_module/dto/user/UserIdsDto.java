package ru.kraser.technical_helper.common_module.dto.user;

import lombok.Builder;

@Builder
public record UserIdsDto(
        String userId,
        String departmentId
) {
}
