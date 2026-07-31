package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(MethodArgumentNotValidException e) {
        log.warn("Ошибка валидации запроса: {}", e.getMessage());
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
            log.warn("Поле '{}' не прошло валидацию: {} (значение: '{}')",
                    error.getField(), error.getDefaultMessage(), error.getRejectedValue());
        });
        log.debug("Всего ошибок валидации: {}", errors.size());
        return errors;
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationException(ValidationException e) {
        log.warn("Ошибка валидации бизнес-логики: {}", e.getMessage());
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFoundException(NotFoundException e) {
        log.warn("Объект не найден: {}", e.getMessage());
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(DuplicateLikeException.class)
    @ResponseStatus(HttpStatus.NO_CONTENT) // заменить на нужный код
    public Map<String, String> handleNotFoundException(DuplicateLikeException e) {
        log.warn("Попытка дублирования данных: {}", e.getMessage());
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("Ошибка чтения JSON: {}", e.getMessage());
        Throwable cause = e.getCause();
        if (cause != null && cause.getMessage() != null) {
            String message = cause.getMessage();
            if (message.contains("Unknown MPA id")) {
                return Map.of("error", message);
            }
            if (message.contains("Unknown genre id")) {
                return Map.of("error", message);
            }
        }
        return Map.of("error", "Неверный формат данных");
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleRuntime(RuntimeException e) {
        log.error("Ошибка выполнения: {}", e.getMessage(), e);
        return Map.of("error", "Произошла непредвиденная ошибка");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleGeneric(Exception e) {
        log.error("Непредвиденная ошибка: {}", e.getMessage(), e);
        return Map.of("error", "Внутренняя ошибка сервера");
    }
}