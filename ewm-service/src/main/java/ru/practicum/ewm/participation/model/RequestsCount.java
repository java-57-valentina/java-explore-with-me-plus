package ru.practicum.ewm.participation.model;

public interface RequestsCount {
    Long getId();    // Должно совпадать с "as id" в запросе
    Integer getCount(); // Должно совпадать с "as count"
}