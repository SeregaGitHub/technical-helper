package ru.kraser.technical_helper.common_module.dto.department;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record DepartmentDto(String id,
                            String name,
                            String createdBy,
                            LocalDateTime createdDate,
                            String lastUpdatedBy,
                            LocalDateTime lastUpdatedDate) {
}
