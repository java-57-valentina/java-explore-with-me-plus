package ru.practicum.ewm.location.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationServiceImpl implements LocationService {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

    @Override
    @Transactional
    public LocationDtoOut addLocationByAdmin(LocationCreateDto dto) {
        Location location = LocationMapper.fromDto(dto);
        location.setState(LocationState.APPROVED);
        Location saved = locationRepository.save(location);
        return LocationMapper.toDto(saved);
    }

    @Override
    @Transactional
    public LocationDtoOut addLocation(Long userId, LocationCreateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));

        Location location = LocationMapper.fromDto(dto);
        location.setCreator(user);
        Location saved = locationRepository.save(location);
        return LocationMapper.toDto(saved);
    }

    @Override
    public Collection<LocationDtoOut> findAll() {

        return locationRepository.findAll().stream()
                .map(LocationMapper::toDto)
                .toList();
    }
}
