package ru.kraser.technical_helper.common_module.dto.breakage;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.BreakageCommentFrontDto;
import ru.kraser.technical_helper.common_module.enums.Priority;
import ru.kraser.technical_helper.common_module.enums.Status;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record BreakageFullDto(
        String id,                        // for employee
        String departmentId,
        String departmentName,            // for employee
        String room,                      // for employee
        String breakageTopic,             // for employee
        String breakageText,              // for employee
        Status status,                    // for employee
        Priority priority,
        String breakageExecutor,
        String executorAppointedBy,
        String createdBy,                // for employee
        @JsonFormat(pattern = "MM-dd-yyyy HH:mm:ss")
        LocalDateTime createdDate,       // for employee
        String lastUpdatedBy,
        @JsonFormat(pattern = "MM-dd-yyyy HH:mm:ss")
        LocalDateTime lastUpdatedDate,
        @JsonFormat(pattern = "MM-dd-yyyy HH:mm:ss")
        LocalDateTime deadline,
        List<BreakageCommentFrontDto> comments
) {
}
