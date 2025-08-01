package ru.practicum.ewm.event.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventAdminFilter;
import ru.practicum.ewm.event.model.EventFilter;
import ru.practicum.ewm.event.service.EventSpecifications;

import java.util.Collection;
import java.util.Optional;

public interface EventRepository extends
        JpaRepository<Event, Long>,
        JpaSpecificationExecutor<Event> {

    @Query(value = """
        SELECT * FROM events
        WHERE initiator_id = :userId
        ORDER BY id
        LIMIT :limit
        OFFSET :offset
        """, nativeQuery = true)
    Collection<Event> findByInitiatorId(
            @Param("userId") Long userId,
            @Param("offset") int offset,
            @Param("limit") int limit);

    @Query(value = """
        SELECT e FROM Event e
        WHERE e.id = :id AND e.state = 'PUBLISHED'
        """)
    Optional<Event> findPublishedById(@Param("id") Long id);

    interface Predicates {
        static Specification<Event> buildSpecification(EventFilter filter) {
            return Specification
                    .where(EventSpecifications.withTextContains(filter.getText()))
                    .and(EventSpecifications.withCategoriesIn(filter.getCategories()))
                    .and(EventSpecifications.withPaid(filter.getPaid()))
                    .and(EventSpecifications.withState(filter.getState()))
                    .and(EventSpecifications.withOnlyAvailable(filter.getOnlyAvailable()))
                    .and(EventSpecifications.withRangeStart(filter.getRangeStart()))
                    .and(EventSpecifications.withRangeEnd(filter.getRangeEnd()));
        }

        static Specification<Event> buildSpecification(EventAdminFilter filter) {
            return Specification
                    .where(EventSpecifications.withUsers(filter.getUsers()))
                    .and(EventSpecifications.withCategoriesIn(filter.getCategories()))
                    .and(EventSpecifications.withStatesIn(filter.getStates()))
                    .and(EventSpecifications.withRangeStart(filter.getRangeStart()))
                    .and(EventSpecifications.withRangeEnd(filter.getRangeEnd()));
        }
    }
}
