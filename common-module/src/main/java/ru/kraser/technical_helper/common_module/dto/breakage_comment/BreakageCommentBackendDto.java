package ru.kraser.technical_helper.common_module.dto.breakage_comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BreakageCommentBackendDto(
        String id,
        String comment,
        String createdBy,
        String creatorName,
        @JsonFormat(pattern = "MM-dd-yyyy HH:mm:ss")
        LocalDateTime createdDate,
        @JsonFormat(pattern = "MM-dd-yyyy HH:mm:ss")
        LocalDateTime lastUpdatedDate
) {
}
