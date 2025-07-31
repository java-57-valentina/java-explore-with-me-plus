package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventDtoOut;
import ru.practicum.ewm.event.dto.EventUpdateAdminDto;
import ru.practicum.ewm.event.model.EventAdminFilter;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.service.EventService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static ru.practicum.ewm.constants.Constants.DATE_TIME_FORMAT;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class AdminEventController {

    private final EventService eventService;

    @GetMapping
    public Collection<EventDtoOut> getEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) List<EventState> states,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "10") Integer limit) {

        log.info("request from Admin: get all events");
        EventAdminFilter filter = EventAdminFilter.builder()
                .users(users)
                .categories(categories)
                .states(states)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(offset)
                .size(limit)
                .build();
        return eventService.findFullEventsBy(filter);
    }

    @PatchMapping("/{eventId}")
    public EventDtoOut updateEvent(
            @PathVariable @Min(1) Long eventId,
            @RequestBody @Valid EventUpdateAdminDto eventDto) {
        log.info("request from Admin: update event:{}", eventId);
        return eventService.update(eventId, eventDto);
    }
}
