package ru.kraser.technical_helper.common_module.dto.breakage_comment;

import jakarta.validation.constraints.NotBlank;

public record CreateBreakageCommentDto(
        @NotBlank(message = "Необходимо заполнить комментарий.")
        String comment
) {
}
