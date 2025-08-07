package ru.practicum.ewm.location.service;

import ru.practicum.ewm.location.dto.*;
import ru.practicum.ewm.location.model.Location;
import ru.practicum.ewm.location.model.LocationAdminFilter;
import ru.practicum.ewm.location.model.LocationPrivateFilter;

import java.util.Collection;

public interface LocationService {

    LocationPrivateDtoOut addLocation(Long userId, LocationCreateDto dto);

    LocationFullDtoOut addLocationByAdmin(LocationCreateDto dto);

    LocationFullDtoOut update(Long id, LocationUpdateAdminDto dto);

    LocationPrivateDtoOut update(Long id, Long userId, LocationUpdateUserDto dto);

    Collection<LocationFullDtoOut> findAllByFilter(LocationAdminFilter filter);

    Collection<LocationPrivateDtoOut> findAllByFilter(Long userId, LocationPrivateFilter filter);

    Collection<LocationDtoOut> findAllApproved();


    void delete(Long id);

    void delete(Long id, Long userId);

    Double getDistance(Long id1, Long id2);

    Location getOrCreateLocation(LocationDto location);

}
