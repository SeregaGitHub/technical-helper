package ru.kraser.technical_helper.common_module.dto.breakage;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.kraser.technical_helper.common_module.enums.Status;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
public class BreakageEmployeeDto {
    private String id;
    private String departmentId;
    private String departmentName;
    private String room;
    private String breakageTopic;
    private String breakageText;
    private Status status;
    private String breakageExecutor;
    private String createdBy;
    @JsonFormat(pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime createdDate;
}
