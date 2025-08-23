package ru.kraser.technical_helper.common_module.dto.breakage;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import ru.kraser.technical_helper.common_module.enums.Priority;
import ru.kraser.technical_helper.common_module.enums.Status;

import java.time.LocalDateTime;

@Builder
public record BreakageDto(
        String id,
        String departmentId,
        String departmentName,
        String room,
        String breakageTopic,
        String breakageText,
        Status status,
        Priority priority,
        String breakageExecutor,
        String executorAppointedBy,
        String createdBy,
        @JsonFormat(pattern = "MM-dd-yyyy HH:mm:ss")
        LocalDateTime createdDate,
        String lastUpdatedBy,
        @JsonFormat(pattern = "MM-dd-yyyy HH:mm:ss")
        LocalDateTime lastUpdatedDate,
        @JsonFormat(pattern = "MM-dd-yyyy HH:mm:ss")
        LocalDateTime deadline
) {
}
