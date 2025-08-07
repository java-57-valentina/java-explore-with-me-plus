package ru.practicum.ewm.location.service;

import jakarta.persistence.criteria.Expression;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.ewm.location.model.Location;
import ru.practicum.ewm.location.model.LocationState;

import java.util.Collection;

@UtilityClass
public class LocationSpecifications {

    public static Specification<Location> withTextContains(String text) {
        if (text == null || text.isBlank())
            return null;

        return (root, query, cb) ->
                cb.or(
                        cb.like(cb.lower(root.get("name")), "%" + text.toLowerCase() + "%"),
                        cb.like(cb.lower(root.get("address")), "%" + text.toLowerCase() + "%")
                );
    }

    public static Specification<Location> withCreatorIn(Collection<Long> users) {
        if (users == null || users.isEmpty())
            return null;

        return (root, query, cb) ->
                root.get("creator").get("id").in(users);
    }

    public static Specification<Location> withState(LocationState state) {
        if (state == null)
            return null;

        return (root, query, cb) ->
                cb.equal(root.get("state"), state);
    }

    public static Specification<Location> withMinEvents(Integer minEvents) {
        return null;
    }

    public static Specification<Location> withMaxEvents(Integer maxEvents) {
        return null;
    }

    public static Specification<Location> withCoordinates(Double lat, Double lon, Double radius) {
        if (lat == null || lon == null)
            return null;

        if (radius == null)
            radius = 0.0;

        Double finalRadius = radius;
        return (root, query, cb) -> {
            Expression<Double> distance = cb.function(
                    "calculate_distance_meters",
                    Double.class,
                    cb.literal(lat),
                    cb.literal(lon),
                    root.get("latitude"),
                    root.get("longitude")
            );
            return cb.lessThanOrEqualTo(distance, finalRadius);
        };
    }
}
