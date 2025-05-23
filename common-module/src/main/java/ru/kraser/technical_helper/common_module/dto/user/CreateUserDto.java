package ru.kraser.technical_helper.common_module.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.kraser.technical_helper.common_module.enums.Role;

public record CreateUserDto (
        @NotBlank(message = "Пользователь должен иметь логин.")
        @Size(min = 4, max = 64, message = "Логин пользователя должен быть от 4 до 64 символов.")
        String username,
        @NotBlank(message = "У пользователя должен быть пароль от его логина.")
        @Size(min = 4, max = 64, message = "Длина пароля должна быть от 4 до 64 символов.")
        String password,
        @NotBlank(message = "Пользователь должен быть сотрудником отдела.")
        String departmentId,
        @NotNull(message = "Пользователь должен иметь свою роль.")
        Role role
) {
}
