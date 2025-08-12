package ru.kraser.technical_helper.common_module.dto.breakage_comment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record BreakageCommentFrontDto(
        String id,
        String comment,
        @JsonProperty(value = "isActionEnabled")
        boolean actionEnabled
) {
}
