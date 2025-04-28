package ru.kraser.technical_helper.common_module.dto.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDepartmentDto(
        @NotBlank(message = "Название отдела не может быть пустым.")
        @Size(min = 4, max = 64, message = "Название отдела должно быть от 4 до 64 символов.")
        String name
) {
}
