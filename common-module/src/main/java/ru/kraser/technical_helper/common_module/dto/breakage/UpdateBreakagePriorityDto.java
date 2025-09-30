package ru.kraser.technical_helper.common_module.dto.breakage;

import jakarta.validation.constraints.NotNull;
import ru.kraser.technical_helper.common_module.enums.Priority;
import ru.kraser.technical_helper.common_module.enums.Status;

public record UpdateBreakagePriorityDto(
        @NotNull(message = "Заявка на неисправность должна иметь корректный приоритет.")
        Priority priority,
        @NotNull(message = "Заявка на неисправность должна иметь корректный статус.")
        Status status
) {
}
