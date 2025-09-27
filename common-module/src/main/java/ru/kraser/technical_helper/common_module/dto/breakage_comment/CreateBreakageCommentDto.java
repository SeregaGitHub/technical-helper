package ru.kraser.technical_helper.common_module.dto.breakage_comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.kraser.technical_helper.common_module.enums.Status;

public record CreateBreakageCommentDto(
        @NotBlank(message = "Необходимо заполнить комментарий.")
        String comment,
        @NotNull(message = "Заявка на неисправность должна иметь корректный статус.")
        Status status
) {
}
