package ru.kraser.technical_helper.gateway.util.error_handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.kraser.technical_helper.common_module.dto.api.ApiError;
import ru.kraser.technical_helper.common_module.exception.AuthException;

import java.net.ConnectException;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GatewayErrorHandler {
    @ExceptionHandler(ConnectException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ResponseEntity<?> handleConnect(ConnectException exception) {
        ApiError error = ApiError.builder()
                .message("Отказано в подключении к: " + exception.getMessage().substring(31))
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error(HttpStatus.SERVICE_UNAVAILABLE)
                .timestamp(LocalDateTime.now().withNano(0))
                .build();
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<?> handleAuthentication(AuthException exception) {
        ApiError error = ApiError.builder()
                .message(exception.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED)
                .timestamp(LocalDateTime.now().withNano(0))
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleNotValid(MethodArgumentNotValidException exception) {
        String message = exception.getMessage();
        int beginIndex = message.lastIndexOf("default message [") + 17;
        int endIndex = message.lastIndexOf(".") + 1;
        String errorMessage = message.substring(beginIndex, endIndex);

        ApiError error = ApiError.builder()
                .message(errorMessage)
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST)
                .timestamp(LocalDateTime.now().withNano(0))
                .build();
        return ResponseEntity.badRequest().body(error);
    }
}
