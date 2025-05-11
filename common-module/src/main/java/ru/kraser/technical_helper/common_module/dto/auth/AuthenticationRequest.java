package ru.kraser.technical_helper.common_module.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AuthenticationRequest(
        @NotBlank(message = "Пользователь должен иметь логин.")
        @Size(min = 4, max = 64, message = "Логин пользователя должен быть от 4 до 64 символов.")
        String username,
        @NotBlank(message = "У пользователя должен быть пароль от его логина.")
        @Size(min = 4, max = 128, message = "Длина пароля должна быть от 4 до 128 символов.")
        String password
) {
}