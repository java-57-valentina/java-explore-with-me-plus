package ru.practicum.ewm.event.service;

import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.model.EventAdminFilter;
import ru.practicum.ewm.event.model.EventFilter;

import java.util.Collection;

public interface EventService {

    EventDtoOut add(Long userId, EventCreateDto eventDto);

    EventDtoOut update(Long userId, Long eventId, EventUpdateDto updateRequest);

    EventDtoOut update(Long eventId, EventUpdateAdminDto eventDto);

    EventDtoOut findBy(Long eventId);

    EventDtoOut get(Long userId, Long eventId);

    Collection<EventShortDtoOut> findShortEventsBy(EventFilter filter);

    Collection<EventDtoOut> findFullEventsBy(EventAdminFilter filter);
    Collection<EventDtoOut> findFullEventsBy(EventFilter filter);

    Collection<EventShortDtoOut> getEventsCreatedByUser(Long userId, Integer offset, Integer limit);

}
