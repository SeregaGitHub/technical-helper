package ru.kraser.technical_helper.common_module.dto.breakage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.kraser.technical_helper.common_module.enums.Status;

import java.time.LocalDate;

public record AppointBreakageExecutorDto(
        @NotBlank(message = "Необходимо назначить исполнителя заявки на неисправность.")
        String executor,
        @NotNull(message = "Необходимо указать срок исполнений заявки на неисправность.")
        LocalDate deadline,
        @NotNull(message = "Заявка на неисправность должна иметь корректный статус.")
        Status status
) {
}
