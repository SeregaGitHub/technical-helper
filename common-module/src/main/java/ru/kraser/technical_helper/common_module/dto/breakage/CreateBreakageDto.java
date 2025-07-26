package ru.kraser.technical_helper.common_module.dto.breakage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.kraser.technical_helper.common_module.enums.Priority;
import ru.kraser.technical_helper.common_module.enums.Status;

public record CreateBreakageDto(
        @NotBlank(message = "Пользователь должен быть сотрудником отдела.")
        String departmentId,
        @NotBlank(message = "Необходимо указать помещение, где произошла неисправность.")
        @Size(min = 1, max = 128, message = "Наименование помещения должено быть от 1 до 128 символов.")
        String room,
        @NotBlank(message = "Необходимо указать тему заявки на неисправность.")
        @Size(min = 1, max = 64, message = "Тема заявки на неисправность должена быть от 1 до 64 символов.")
        String breakageTopic,
        @NotBlank(message = "Необходимо описать неисправность.")
        @Size(min = 1, max = 64, message = "Описание неисправности должено быть от 1 до 2048 символов.")
        String breakageText,
        @NotNull(message = "У заявки на неисправность должен быть свой статус.")
        Status status,
        @NotNull(message = "У заявки на неисправность должен быть свой приоритет.")
        Priority priority
) {
}
