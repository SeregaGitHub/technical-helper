package ru.kraser.technical_helper.common_module.dto.breakage;

import jakarta.validation.constraints.NotNull;
import ru.kraser.technical_helper.common_module.enums.Priority;

public record UpdateBreakagePriorityDto(
        @NotNull(message = "Заявка на неисправность должна иметь корректный приоритет.")
        Priority priority
) {
}
