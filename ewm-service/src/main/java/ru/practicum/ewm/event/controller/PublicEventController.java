package ru.practicum.ewm.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventDtoOut;
import ru.practicum.ewm.event.dto.EventShortDtoOut;
import ru.practicum.ewm.event.model.EventFilter;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.exception.InvalidRequestException;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.statsclient.StatsClientException;
import ru.practicum.statsdto.StatsDtoOut;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.ewm.constants.Constants.DATE_TIME_FORMAT;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class PublicEventController {

    private final EventService eventService;
    private final StatsClient statsClient;

    @Value("${spring.application.name:ewm}")
    private String appName;

    // Получение событий с возможностью фильтрации
    @GetMapping
    public Collection<EventShortDtoOut> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(defaultValue = "EVENT_DATE") String sort,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {

        EventFilter filter = EventFilter.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .from(from)
                .size(size)
                .state(EventState.PUBLISHED)
                .build();

        if (filter.getRangeStart() != null && filter.getRangeEnd() != null
                && filter.getRangeStart().isAfter(filter.getRangeEnd())) {
            throw new InvalidRequestException("The start date of the range must be earlier than the end date.");
        }

        Collection<EventShortDtoOut> events = eventService.findShortEventsBy(filter);

        enrichWithStatistics(events);

        Collection<Long> ids = events.stream()
                .map(EventShortDtoOut::getId)
                .toList();

        writeStatisticsByIds(ids, request.getRemoteAddr());
        writeStatisticsByUris(List.of("/events"), request.getRemoteAddr());

        return events;
    }

    @GetMapping("/{eventId}")
    public EventDtoOut get(@PathVariable @Min(1) Long eventId,
                           HttpServletRequest request) {

        log.debug("request for published event id:{}", eventId);
        EventDtoOut dtoOut = eventService.findBy(eventId);

        Map<String, Integer> hits = getStatistics(List.of(eventId));
        dtoOut.setViews(hits.getOrDefault("/events/" + eventId, 0));

        writeStatisticsByIds(List.of(eventId), request.getRemoteAddr());

        return dtoOut;
    }


    private void enrichWithStatistics(Collection<EventShortDtoOut> events) {
        if (events.isEmpty())
            return;

        Collection<Long> ids = events.stream()
                .map(EventShortDtoOut::getId)
                .toList();

        Map<String, Integer> hits = getStatistics(ids);
        if (hits.isEmpty())
            return;

        events.forEach(dto ->
                dto.setViews(hits.getOrDefault("/events/" + dto.getId(), 0))
        );
    }

    private void writeStatisticsByIds(Collection<Long> ids, String ip) {
        writeStatisticsByUris(ids.stream().map(id -> "/events/" + id).toList(), ip);
    }

    private void writeStatisticsByUris(Collection<String> uris, String ip) {
        try {
            for (String uri : uris)
                statsClient.hit(appName, uri, ip);

        } catch (StatsClientException ex) {
            log.error(ex.getMessage());
        }
    }

    private Map<String, Integer> getStatistics(Collection<Long> ids) {
        Collection<StatsDtoOut> stats = List.of();

        try {
            stats = statsClient.getStats(
                    LocalDateTime.now().minusYears(10),
                    LocalDateTime.now().plusYears(10),
                    ids.stream().map(id -> "/events/" + id).toList(),
                    true);
        } catch (StatsClientException ex) {
            log.error(ex.getMessage());
        }

        if (stats.isEmpty())
            return Map.of();

        return stats.stream()
                .collect(Collectors.toMap(
                        StatsDtoOut::getUri,
                        StatsDtoOut::getHits
                ));
    }
}
