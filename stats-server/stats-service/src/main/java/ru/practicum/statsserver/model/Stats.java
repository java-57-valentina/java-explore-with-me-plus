package ru.practicum.statsserver.model;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Stats {
    private String service;
    private String uri;
    private int hits;

    public Stats(String service, String uri, long hits) {
        this.service = service;
        this.uri = uri;
        this.hits = (int) hits;
    }
}