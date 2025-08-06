package ru.practicum.ewm.location.service;

import ru.practicum.ewm.location.dto.*;
import ru.practicum.ewm.location.model.Location;

import java.util.Collection;

public interface LocationService {

    LocationDtoOut addLocation(Long userId, LocationCreateDto dto);

    LocationFullDtoOut addLocationByAdmin(LocationCreateDto dto);

    LocationFullDtoOut update(Long id, LocationUpdateAdminDto dto);

    LocationDtoOut update(Long id, Long userId, LocationUpdateUserDto dto);

    Collection<LocationFullDtoOut> findAll();

    void delete(Long id);

    void delete(Long id, Long userId);

    Double getDistance(Long id1, Long id2);

    Location getOrCreateLocation(LocationDto location);
}
