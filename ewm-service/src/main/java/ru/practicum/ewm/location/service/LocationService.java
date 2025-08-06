package ru.practicum.ewm.location.service;

import ru.practicum.ewm.location.dto.LocationCreateDto;
import ru.practicum.ewm.location.dto.LocationDtoOut;

import java.util.Collection;

public interface LocationService {

    LocationDtoOut  addLocation(Long userId, LocationCreateDto dto);

    LocationDtoOut addLocationByAdmin(LocationCreateDto dto);

    Collection<LocationDtoOut> findAll();
}
