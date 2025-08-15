package ru.kraser.technical_helper.common_module.dto.breakage_comment;

import lombok.Builder;

@Builder
public record BreakageCommentBackendDto(
        String id,
        String comment,
        String createdBy
) {
}
