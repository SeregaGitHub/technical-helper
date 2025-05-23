package ru.kraser.technical_helper.common_module.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeUserPasswordDto(
        @NotBlank(message = "У пользователя должен быть пароль от его логина.")
        @Size(min = 4, max = 64, message = "Длина пароля должна быть от 4 до 64 символов.")
        String newPassword
) {
}
