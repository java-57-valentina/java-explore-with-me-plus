package ru.practicum.ewm.location.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.exception.ConditionNotMetException;
import ru.practicum.ewm.exception.DuplicateLocationsException;
import ru.practicum.ewm.exception.NoAccessException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.location.dto.*;
import ru.practicum.ewm.location.mapper.LocationMapper;
import ru.practicum.ewm.location.model.Location;
import ru.practicum.ewm.location.model.LocationAdminFilter;
import ru.practicum.ewm.location.model.LocationState;
import ru.practicum.ewm.location.repository.LocationRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationServiceImpl implements LocationService {

    private static final double NEARBY_RADIUS = 50; // meters

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

    @Override
    @Transactional
    public LocationFullDtoOut addLocationByAdmin(LocationCreateDto dto) {
        Location location = LocationMapper.fromDto(dto);
        location.setState(LocationState.APPROVED);
        return LocationMapper.toFullDto(locationRepository.save(location));
    }

    @Override
    @Transactional
    public LocationDtoOut addLocation(Long userId, LocationCreateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));

        Optional<Location> existing = locationRepository.findDuplicates(
                dto.getName(),
                dto.getLatitude(),
                dto.getLongitude(),
                NEARBY_RADIUS);

        if (existing.isPresent()) {
            log.warn("Nearby location: {}", existing.get());
            throw new DuplicateLocationsException(getDuplicateErrorMessage(existing.get()));
        }

        Location location = LocationMapper.fromDto(dto);
        location.setCreator(user);
        Location saved = locationRepository.save(location);
        return LocationMapper.toDto(saved);
    }

    @Override
    @Transactional
    public LocationFullDtoOut update(Long id, LocationUpdateAdminDto dto) {
        log.info("try update location by admin: {}", dto);
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Location", id));

        Optional.ofNullable(dto.getName()).ifPresent(location::setName);
        Optional.ofNullable(dto.getAddress()).ifPresent(location::setAddress);
        Optional.ofNullable(dto.getLatitude()).ifPresent(location::setLatitude);
        Optional.ofNullable(dto.getLongitude()).ifPresent(location::setLongitude);
        Optional.ofNullable(dto.getState()).ifPresent(
                state -> changeLocationState(location, state));

        return LocationMapper.toFullDto(location);
    }

    @Override
    @Transactional
    public LocationFullDtoOut update(Long id, Long userId, LocationUpdateUserDto dto) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Location", id));

        if (location.getState() != LocationState.PENDING) {
            throw new ConditionNotMetException("Cannot update published or rejected location");
        }

        if (location.getCreator() == null || !location.getCreator().getId().equals(userId)) {
            throw new NoAccessException("Only creator can edit this location");
        }

        Optional.ofNullable(dto.getName()).ifPresent(location::setName);
        Optional.ofNullable(dto.getAddress()).ifPresent(location::setAddress);
        Optional.ofNullable(dto.getLatitude()).ifPresent(location::setLatitude);
        Optional.ofNullable(dto.getLongitude()).ifPresent(location::setLongitude);

        return LocationMapper.toFullDto(location);
    }


    private void changeLocationState(Location location, LocationState state) {
        log.info("changeLocationState id:{} state: {} -> {}", location.getId(), location.getState(), state);
        if (location.getState() == state)
            return;

        if (state == LocationState.PENDING || state == LocationState.AUTO_GENERATED) {
            throw new ConditionNotMetException(
                    String.format("Cannot change state from %s to %s", location.getState(), state));
        }
        location.setState(state);
    }

    private static String getDuplicateErrorMessage(@NotNull Location existing) {
        Long id = existing.getId();
        switch (existing.getState()) {
            case LocationState.APPROVED -> {
                return String.format("Please use existing location (id=%d)", id);
            }
            case LocationState.PENDING -> {
                return String.format("A request to create this location already exists (id=%d). Please wait for approval.", id);
            }
            case LocationState.REJECTED -> {
                return  "The request for creating this location was rejected earlier. Please contact admin.";
            }
        }
        return "";
    }

    @Override
    public Collection<LocationFullDtoOut> findAllByFilter(LocationAdminFilter filter) {
        Specification<Location> spec = buildSpecification(filter);
        List<Location> locations = locationRepository.findAll(spec, filter.getPageable()).getContent();
        return locations.stream()
                .map(LocationMapper::toFullDto)
                .toList();
    }

    @Override
    public Collection<LocationDtoOut> findAllApproved() {
        return locationRepository.findAllByState(LocationState.APPROVED).stream()
                .map(LocationMapper::toDto)
                .toList();
    }

    @Override
    public Collection<LocationFullDtoOut> findAllByUser(Long userId) {
        return locationRepository.findAllByCreatorId(userId).stream()
                .map(LocationMapper::toFullDto)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (existEventsInLocation(id)) {
            throw new ConditionNotMetException("There are events in this location");
        }
        locationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Location", id));

        if (location.getState() == LocationState.APPROVED) {
            throw new ConditionNotMetException("Cannot delete published location");
        }

        if (location.getCreator() == null || !location.getCreator().getId().equals(userId)) {
            throw new NoAccessException("Only creator can delete this location");
        }

        if (existEventsInLocation(id)) {
            throw new ConditionNotMetException("There are events in this location");
        }

        locationRepository.deleteById(id);
    }

    @Override
    public Double getDistance(Long id1, Long id2) {
        if (id1.equals(id2))
            return 0.0;

        Location location1 = locationRepository.findById(id1)
                .orElseThrow(() -> new NotFoundException("Location", id1));

        Location location2 = locationRepository.findById(id2)
                .orElseThrow(() -> new NotFoundException("Location", id2));

        return locationRepository.calculateDistanceInMeters(
                location1.getLatitude(), location1.getLongitude(),
                location2.getLatitude(), location2.getLongitude());
    }

    @Override
    public Location getOrCreateLocation(LocationDto location) {

        if (location.getId() != null) {
            return locationRepository.findByIdAndState(location.getId(), LocationState.APPROVED)
                    .orElseThrow(() -> new NotFoundException("Location", location.getId()));
        }

        if (location.getLatitude() != null && location.getLongitude() != null) {
            Optional<Location> nearByAutoGenerated = locationRepository.findNearByAutoGenerated(
                    location.getLatitude(), location.getLongitude());

            return nearByAutoGenerated.orElseGet(()
                    -> createAutoGeneratedLocation(location.getLatitude(), location.getLongitude()));
        }

        throw new ConditionNotMetException("Invalid location");
    }

    @Transactional
    private Location createAutoGeneratedLocation(Double lat, Double lon) {
        Location location = Location.builder()
                .latitude(lat)
                .longitude(lon)
                .state(LocationState.AUTO_GENERATED)
                .build();
        return locationRepository.save(location);
    }

    private boolean existEventsInLocation(Long id) {
        // Будет реализовано в рамках задачи #60
        return false;
    }

    private Specification<Location> buildSpecification(LocationAdminFilter filter) {
        return Stream.of(
                        optionalSpec(LocationSpecifications.withTextContains(filter.getText())),
                        optionalSpec(LocationSpecifications.withCreatorIn(filter.getUsers())),
                        optionalSpec(LocationSpecifications.withCoordinates(filter.getLat(), filter.getLon(), filter.getRadius())),
                        optionalSpec(LocationSpecifications.withState(filter.getState())),
                        optionalSpec(LocationSpecifications.withMinEvents(filter.getMinEvents())),
                        optionalSpec(LocationSpecifications.withMaxEvents(filter.getMaxEvents()))
                )
                .filter(Objects::nonNull)
                .reduce(Specification::and)
                .orElse((root, query, cb) -> cb.conjunction());
    }

    private static <T> Specification<T> optionalSpec(Specification<T> spec) {
        return spec;
    }
}
