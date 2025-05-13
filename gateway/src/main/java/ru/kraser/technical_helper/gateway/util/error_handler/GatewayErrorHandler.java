package ru.kraser.technical_helper.gateway.util.error_handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.kraser.technical_helper.common_module.dto.api.ApiError;
import ru.kraser.technical_helper.common_module.exception.AlreadyExistsException;
import ru.kraser.technical_helper.common_module.exception.AuthException;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;

import java.net.ConnectException;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GatewayErrorHandler {
    @ExceptionHandler(ConnectException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ResponseEntity<?> handleNoConnect(ConnectException exception) {
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
    public ResponseEntity<?> handleAuthenticationError(AuthException exception) {
        ApiError error = ApiError.builder()
                .message(exception.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED)
                .timestamp(LocalDateTime.now().withNano(0))
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(WebClientResponseException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<?> handleNoAccess(WebClientResponseException exception) {
        ApiError error = ApiError.builder()
                .message("У Вас нет прав доступа для данного ресурса.")
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN)
                .timestamp(LocalDateTime.now().withNano(0))
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<?> handleNoAuthenticationToken(MissingRequestHeaderException exception) {
        ApiError error = ApiError.builder()
                .message("Вы должны пройти аутентификацию.")
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED)
                .timestamp(LocalDateTime.now().withNano(0))
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleNotValidArgument(MethodArgumentNotValidException exception) {
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleNotDeserialized(HttpMessageNotReadableException exception) {
        String message;
        if (exception.getMessage().contains("enums.Role")) {
            message = "Выберите одно из значений для роли пользователя - [TECHNICIAN, EMPLOYEE, ADMIN] !!!";
        } else {
            message = "Ошибка синтаксического анализа JSON: не удается десериализовать переданное значение !!!";
        }

        ApiError error = ApiError.builder()
                .message(message)
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST)
                .timestamp(LocalDateTime.now().withNano(0))
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<?> handleAlreadyExists(AlreadyExistsException exception) {
        ApiError error = ApiError.builder()
                .message(exception.getMessage())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .error(HttpStatus.UNPROCESSABLE_ENTITY)
                .timestamp(LocalDateTime.now().withNano(0))
                .build();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<?> handleNotFound(NotFoundException exception) {
        ApiError error = ApiError.builder()
                .message(exception.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND)
                .timestamp(LocalDateTime.now().withNano(0))
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
