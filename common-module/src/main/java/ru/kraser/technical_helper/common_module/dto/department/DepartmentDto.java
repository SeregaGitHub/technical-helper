package ru.kraser.technical_helper.common_module.dto.department;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record DepartmentDto(
        String id,
        String name,
        String createdBy,
        //@JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
        @JsonFormat(pattern = "MM-dd-yyyy HH:mm:ss")
        LocalDateTime createdDate,
        String lastUpdatedBy,
        @JsonFormat(pattern = "MM-dd-yyyy HH:mm:ss")
        LocalDateTime lastUpdatedDate
) {
}
