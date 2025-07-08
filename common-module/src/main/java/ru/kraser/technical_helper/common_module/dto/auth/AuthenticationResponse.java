package ru.kraser.technical_helper.common_module.dto.auth;

import lombok.Builder;
import ru.kraser.technical_helper.common_module.enums.Role;

@Builder
public record AuthenticationResponse(
        String thJwt,
        String username,
        Role role
) {
}