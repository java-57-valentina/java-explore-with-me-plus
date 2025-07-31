package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventDtoOut;
import ru.practicum.ewm.event.dto.EventUpdateAdminDto;
import ru.practicum.ewm.event.model.EventFilter;
import ru.practicum.ewm.event.service.EventService;

import java.util.Collection;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class AdminEventController {

    private final EventService eventService;

    @GetMapping
    public Collection<EventDtoOut> getEvents(
            @RequestParam(name = "from", defaultValue = "0") @Min(0) Integer offset,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) Integer limit) {

        log.info("request from Admin: get all events");
        EventFilter filter = EventFilter.builder()
                .state(null)
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
