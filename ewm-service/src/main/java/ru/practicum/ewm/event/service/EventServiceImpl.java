package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventAdminFilter;
import ru.practicum.ewm.event.model.EventFilter;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConditionNotMetException;
import ru.practicum.ewm.exception.NoAccessException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private static final int MIN_TIME_TO_UNPUBLISHED_EVENT = 2;
    private static final int MIN_TIME_TO_PUBLISHED_EVENT = 1;

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;


    @Override
    public EventDtoOut add(Long userId, EventCreateDto eventDto) {

        validateEventDate(eventDto.getEventDate(), EventState.PENDING);
        Category category = getCategory(eventDto.getCategoryId());
        User user = getUser(userId);

        Event event = EventMapper.fromDto(eventDto);
        event.setCategory(category);
        event.setInitiator(user);

        event = eventRepository.save(event);

        return EventMapper.toDto(event);
    }

    @Override
    public EventDtoOut update(Long userId, Long eventId, EventUpdateDto eventDto) {

        Event event = getEvent(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NoAccessException("Only initiator can edit the event");
        }

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConditionNotMetException("Cannot update published event");
        }

        Optional.ofNullable(eventDto.getTitle()).ifPresent(event::setTitle);
        Optional.ofNullable(eventDto.getAnnotation()).ifPresent(event::setAnnotation);
        Optional.ofNullable(eventDto.getDescription()).ifPresent(event::setDescription);
        Optional.ofNullable(eventDto.getPaid()).ifPresent(event::setPaid);
        Optional.ofNullable(eventDto.getLocation()).ifPresent(loc -> {
            event.setLocationLat(loc.getLat());
            event.setLocationLon(loc.getLon());
        });
        Optional.ofNullable(eventDto.getParticipantLimit()).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(eventDto.getRequestModeration()).ifPresent(event::setRequestModeration);

        if (eventDto.getCategoryId() != null
                && !eventDto.getCategoryId().equals(event.getCategory().getId())) {
            Category category = categoryRepository.findById(eventDto.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category", eventDto.getCategoryId()));
            event.setCategory(category);
        }

        if (eventDto.getEventDate() != null) {
            validateEventDate(eventDto.getEventDate(), event.getState());
            event.setEventDate(eventDto.getEventDate());
        }

        if (eventDto.getStateAction() != null) {
            switch (eventDto.getStateAction()) {
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
                case CANCEL_REVIEW  -> event.setState(EventState.CANCELED);
            }
        }

        Event updated = eventRepository.save(event);
        return EventMapper.toDto(updated);
    }

    @Override
    public EventDtoOut update(Long eventId, EventUpdateAdminDto eventDto) {

        // дата начала изменяемого события должна быть не ранее чем за час от даты публикации.
        // (Ожидается код ошибки 409)

        Event event = getEvent(eventId);

        Optional.ofNullable(eventDto.getTitle()).ifPresent(event::setTitle);
        Optional.ofNullable(eventDto.getAnnotation()).ifPresent(event::setAnnotation);
        Optional.ofNullable(eventDto.getDescription()).ifPresent(event::setDescription);
        Optional.ofNullable(eventDto.getParticipantLimit()).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(eventDto.getPaid()).ifPresent(event::setPaid);
        Optional.ofNullable(eventDto.getLocation()).ifPresent(loc -> {
            event.setLocationLat(loc.getLat());
            event.setLocationLon(loc.getLon());
        });
        Optional.ofNullable(eventDto.getParticipantLimit()).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(eventDto.getRequestModeration()).ifPresent(event::setRequestModeration);

        if (eventDto.getEventDate() != null) {
            validateEventDate(eventDto.getEventDate(), event.getState());
            event.setEventDate(eventDto.getEventDate());
        }

        if (eventDto.getStateAction() != null) {
            switch (eventDto.getStateAction()) {
                case PUBLISH_EVENT -> publishEvent(event);
                case REJECT_EVENT -> rejectEvent(event);
            }
        }

        Event saved = eventRepository.save(event);
        return EventMapper.toDto(saved);
    }

    @Override
    public EventDtoOut findBy(Long eventId) {
        Event existing = eventRepository.findPublishedById(eventId)
                .orElseThrow(() -> new NotFoundException("Event", eventId));

        return EventMapper.toDto(existing);
    }

    @Override
    public EventDtoOut get(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));

        Event existing = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event", eventId));

        if (!existing.getInitiator().getId().equals(userId)) {
            throw new NoAccessException("Only initiator can edit the event");
        }

        return EventMapper.toDto(existing);
    }

    @Override
    public Collection<EventShortDtoOut> findShortEventsBy(EventFilter filter) {
        return findBy(filter).stream()
                .map(EventMapper::toShortDto)
                .toList();
    }

    @Override
    public Collection<EventDtoOut> findFullEventsBy(EventAdminFilter filter) {
        return findBy(filter).stream()
                .map(EventMapper::toDto)
                .toList();
    }


    private Collection<Event> findBy(EventFilter filter) {
        log.debug("findBy EventFilter: {}", filter);
        Specification<Event> spec = buildSpecification(filter);
        return eventRepository.findAll(spec, filter.getPageable()).getContent();
    }

    private Collection<Event> findBy(EventAdminFilter filter) {
        log.debug("findBy EventAdminFilter: {}", filter);
        Specification<Event> spec = buildSpecification(filter);
        return eventRepository.findAll(spec, filter.getPageable()).getContent();
    }

    private Specification<Event> buildSpecification(EventAdminFilter filter) {
        Specification<Event> spec = Specification
                .where(EventSpecifications.withUsers(filter.getUsers()))
                .and(EventSpecifications.withCategoriesIn(filter.getCategories()))
                .and(EventSpecifications.withStatesIn(filter.getStates()))
                .and(EventSpecifications.withRangeStart(filter.getRangeStart()))
                .and(EventSpecifications.withRangeEnd(filter.getRangeEnd()));

        return spec;
    }

    private Specification<Event> buildSpecification(EventFilter filter) {
        Specification<Event> spec = Specification
                .where(EventSpecifications.withTextContains(filter.getText()))
                .and(EventSpecifications.withCategoriesIn(filter.getCategories()))
                .and(EventSpecifications.withPaid(filter.getPaid()))
                .and(EventSpecifications.withState(filter.getState()))
                .and(EventSpecifications.withOnlyAvailable(filter.getOnlyAvailable()))
                .and(EventSpecifications.withRangeStart(filter.getRangeStart()))
                .and(EventSpecifications.withRangeEnd(filter.getRangeEnd()));

        return spec;
    }


    @Override
    public Collection<EventShortDtoOut> getEventsCreatedByUser(Long userId, Integer offset, Integer limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));

        return eventRepository.findByInitiatorId(userId, offset, limit).stream()
                .map(EventMapper::toShortDto)
                .toList();
    }

    private void validateEventDate(LocalDateTime eventDate, EventState state) {
        if (eventDate == null) {
            throw new IllegalArgumentException("eventDate is null");
        }

        int hours = state == EventState.PUBLISHED
                ? MIN_TIME_TO_PUBLISHED_EVENT
                : MIN_TIME_TO_UNPUBLISHED_EVENT;

        if (eventDate.isBefore(LocalDateTime.now().plusHours(hours))) {
            String message = "The event date must be no earlier than %d hours from the %s time"
                .formatted(hours, state == EventState.PUBLISHED ? "publishing" : "current");
            throw new ConditionNotMetException(message);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category", categoryId));
    }

    @SuppressWarnings("UnusedReturnValue")
    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));
    }

    @SuppressWarnings("UnusedReturnValue")
    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event", eventId));
    }


    private void publishEvent(Event event) {
        if (event.getState() != EventState.PENDING) {
            throw new ConditionNotMetException("Событие можно публиковать, только если оно в состоянии ожидания публикации");
        }

        validateEventDate(event.getEventDate(), EventState.PUBLISHED);

        event.setState(EventState.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now());
    }

    private void rejectEvent(Event event) {
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConditionNotMetException("Событие можно отклонить, только если оно еще не опубликовано");
        }
        event.setState(EventState.CANCELED);
    }
}
