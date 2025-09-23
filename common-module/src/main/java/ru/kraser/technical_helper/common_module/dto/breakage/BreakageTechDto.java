package ru.kraser.technical_helper.common_module.dto.breakage;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import ru.kraser.technical_helper.common_module.enums.Priority;
import ru.kraser.technical_helper.common_module.enums.Status;

import java.time.LocalDateTime;

@Builder
public record BreakageTechDto(
        String id,
        String departmentId,
        String departmentName,
        String room,
        String breakageTopic,
        String breakageText,
        Status status,
        Priority priority,
        String breakageExecutor,
        String createdBy,
        @JsonFormat(pattern = "MM-dd-yyyy HH:mm:ss")
        LocalDateTime createdDate
) {
}
