package ru.practicum.ewm.location.service;

import jakarta.validation.constraints.Min;
import ru.practicum.ewm.location.dto.*;
import ru.practicum.ewm.location.model.Location;

import java.util.Collection;

public interface LocationService {

    LocationDtoOut addLocation(Long userId, LocationCreateDto dto);

    LocationFullDtoOut addLocationByAdmin(LocationCreateDto dto);

    LocationFullDtoOut update(Long id, LocationUpdateAdminDto dto);

    LocationFullDtoOut update(Long id, Long userId, LocationUpdateUserDto dto);

    Collection<LocationFullDtoOut> findAll();

    Collection<LocationDtoOut> findAllApproved();

    Collection<LocationFullDtoOut> findAllByUser(@Min(1) Long userId);

    void delete(Long id);

    void delete(Long id, Long userId);

    Double getDistance(Long id1, Long id2);

    Location getOrCreateLocation(LocationDto location);

}
