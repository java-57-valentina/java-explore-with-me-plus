package ru.practicum.ewm.location.service;

import ru.practicum.ewm.location.dto.LocationCreateDto;
import ru.practicum.ewm.location.dto.LocationDtoOut;
import ru.practicum.ewm.location.dto.LocationUpdateAdminDto;
import ru.practicum.ewm.location.dto.LocationUpdateUserDto;

import java.util.Collection;

public interface LocationService {

    LocationDtoOut addLocation(Long userId, LocationCreateDto dto);

    LocationDtoOut addLocationByAdmin(LocationCreateDto dto);

    LocationDtoOut update(Long id, LocationUpdateAdminDto dto);

    LocationDtoOut update(Long id, Long userId, LocationUpdateUserDto dto);

    Collection<LocationDtoOut> findAll();

    void delete(Long id);

    void delete(Long id, Long userId);

    Double getDistance(Long id1, Long id2);

}
