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
        String departmentName,      // emp
        String room,                // emp
        String breakageTopic,       // emp
        String breakageText,
        Status status,              // emp
        Priority priority,          // emp
        String executor,            // emp
        String executorAppointedBy,
        String createdBy,           // emp
        @JsonFormat(pattern = "MM-dd-yyyy HH:mm:ss")
        LocalDateTime createdDate,  // emp
        String lastUpdatedBy,
        @JsonFormat(pattern = "MM-dd-yyyy HH:mm:ss")
        LocalDateTime lastUpdatedDate,
        @JsonFormat(pattern = "MM-dd-yyyy HH:mm:ss")
        LocalDateTime deadline
) {
}
