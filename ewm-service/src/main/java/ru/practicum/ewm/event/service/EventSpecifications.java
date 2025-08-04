package ru.practicum.ewm.event.service;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

@UtilityClass
public class EventSpecifications {

    public static Specification<Event> withTextContains(String text) {
        if (text == null || text.isBlank())
            return null;

        return (root, query, cb) ->
                cb.or(
                        cb.like(cb.lower(root.get("title")), "%" + text.toLowerCase() + "%"),
                        cb.like(cb.lower(root.get("annotation")), "%" + text.toLowerCase() + "%"),
                        cb.like(cb.lower(root.get("description")), "%" + text.toLowerCase() + "%")
                );
    }

    public static Specification<Event> withUsers(List<Long> users) {
        if (users == null || users.isEmpty())
            return null;

        return (root, query, cb) ->
                root.get("initiator").get("id").in(users);
    }

    public static Specification<Event> withCategoriesIn(List<Long> categories) {
        if (categories == null || categories.isEmpty())
            return null;

        return (root, query, cb) ->
                root.get("category").get("id").in(categories);
    }

    public static Specification<Event> withStatesIn(List<EventState> states) {
        if (states == null || states.isEmpty())
            return null;

        return (root, query, cb) ->
                root.get("state").in(states);
    }

    public static Specification<Event> withPaid(Boolean paid) {
        if (paid == null)
            return null;

        return (root, query, cb) ->
                cb.equal(root.get("paid"), paid);
    }

    public static Specification<Event> withState(EventState state) {
        if (state == null)
            return null;

        return (root, query, cb) ->
                cb.equal(root.get("state"), state);
    }

    public static Specification<Event> withOnlyAvailable(Boolean onlyAvailable) {
        if (onlyAvailable == null || !onlyAvailable) {
            return null;
        }
        return (root, query, cb) -> cb.or(
                cb.equal(root.get("participantLimit"), 0),  // Нет лимита
                cb.greaterThan(
                        root.get("participantLimit"),
                        root.get("confirmedRequests")
                )
        );
    }

    public static Specification<Event> withRangeStart(LocalDateTime rangeStart) {
        return rangeStart == null ? null : (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart);
    }

    public static Specification<Event> withRangeEnd(LocalDateTime rangeEnd) {
        return rangeEnd == null ? null : (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd);
    }

}