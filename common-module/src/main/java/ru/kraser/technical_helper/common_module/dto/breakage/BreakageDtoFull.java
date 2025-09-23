package ru.kraser.technical_helper.common_module.dto.breakage;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.BreakageCommentFrontDto;
import ru.kraser.technical_helper.common_module.enums.Priority;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Value
public class BreakageDtoFull extends BreakageShortDto {
    String breakageExecutorId;
    Priority priority;
    String executorAppointedBy;
    String lastUpdatedBy;
    @JsonFormat(pattern = "MM-dd-yyyy HH:mm:ss")
    LocalDateTime lastUpdatedDate;
    LocalDateTime deadline;
    List<BreakageCommentFrontDto> comments;
}
