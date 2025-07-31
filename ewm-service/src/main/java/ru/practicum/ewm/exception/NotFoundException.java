package ru.practicum.ewm.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String entity, Long id) {
        super(String.format("Объект %s с id=%d не найден.", entity, id));
    }
}