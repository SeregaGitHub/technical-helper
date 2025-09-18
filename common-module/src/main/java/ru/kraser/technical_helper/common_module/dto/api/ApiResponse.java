package ru.kraser.technical_helper.common_module.dto.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Builder
public record ApiResponse(
    String message,
    int status,
    HttpStatus httpStatus,
    @JsonFormat(pattern = "MM-dd-yyyy HH:mm:ss")
    LocalDateTime timestamp,
    Object data
) {
}
