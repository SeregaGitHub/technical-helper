package ru.kraser.technical_helper.common_module.dto.breakage_comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BreakageCommentFrontDto(
        String id,
        String comment,
        @JsonProperty(value = "isActionEnabled")
        boolean actionEnabled,
        String creatorName,
        @JsonFormat(pattern = "MM-dd-yyyy HH:mm:ss")
        LocalDateTime createdDate,
        @JsonFormat(pattern = "MM-dd-yyyy HH:mm:ss")
        LocalDateTime lastUpdatedDate
) {
}
