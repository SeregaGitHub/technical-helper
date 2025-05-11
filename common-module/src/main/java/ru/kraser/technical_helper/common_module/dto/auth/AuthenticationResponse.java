package ru.kraser.technical_helper.common_module.dto.auth;

import lombok.Builder;

@Builder
public record AuthenticationResponse(
        String token
) {
}