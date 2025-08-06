package ru.practicum.ewm.location.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.exception.ConditionNotMetException;
import ru.practicum.ewm.exception.DuplicateLocationsException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.location.dto.LocationCreateDto;
import ru.practicum.ewm.location.dto.LocationDtoOut;
import ru.practicum.ewm.location.mapper.LocationMapper;
import ru.practicum.ewm.location.model.Location;
import ru.practicum.ewm.location.model.LocationState;
import ru.practicum.ewm.location.repository.LocationRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.Collection;
import java.util.Optional;

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
    public LocationDtoOut addLocationByAdmin(LocationCreateDto dto) {
        Location location = LocationMapper.fromDto(dto);
        location.setState(LocationState.APPROVED);
        return LocationMapper.toDto(locationRepository.save(location));
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
    public Collection<LocationDtoOut> findAll() {

        return locationRepository.findAll().stream()
                .map(LocationMapper::toDto)
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

    private boolean existEventsInLocation(Long id) {
        // Будет реализовано в рамках задачи #60
        return false;
    }
}
