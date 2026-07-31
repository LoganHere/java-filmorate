package ru.yandex.practicum.filmorate.exception;

public class DuplicateLikeException extends RuntimeException {
    public DuplicateLikeException(String message) {
        super(message);
    }
}