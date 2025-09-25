package ru.kraser.technical_helper.common_module.dto.api;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record AppPage(
        List<?> content,
        Long totalElements,
        Integer totalPages,
        Integer numberOfElements,
        Integer pageNumber,
        Integer pageSize,
        Long offset,
        boolean first,
        boolean last,
        boolean isForEmployee,
        LocalDateTime now
) {
}
