package ru.kraser.technical_helper.common_module.dto.department;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record DepartmentDto(String id,
                            String name,
                            String createdBy,
                            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                            LocalDateTime createdDate,
                            String lastUpdatedBy,
                            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                            LocalDateTime lastUpdatedDate) {
}
