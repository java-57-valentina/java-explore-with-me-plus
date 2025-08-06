package ru.practicum.ewm.location.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.NotImplementedException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.location.dto.LocationCreateDto;
import ru.practicum.ewm.location.dto.LocationDtoOut;
import ru.practicum.ewm.location.service.LocationService;

import java.util.Collection;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/locations")
public class PrivateLocationController {

    private final LocationService locationService;

    /**
     * Создать локацию от имени пользователя.
     * Локация автоматически переводится в статус PENDING
     * @return DTO созданной локации
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    LocationDtoOut create(@PathVariable @Min(1) Long userId,
                          @RequestBody @Valid LocationCreateDto dto) {
        log.debug("request for adding location {} from user: {}", dto, userId);
        return locationService.addLocation(userId, dto);
    }

    /**
     * Редактировать существующую локацию от имени создателя.
     * Локация автоматически переводится в статус PENDING
     * @return DTO обновленной локации
     */
    // TODO:
    @PatchMapping("/{id}")
    Collection<LocationDtoOut> update(@PathVariable @Min(1) Long userId,
                                      @PathVariable @Min(1) Long id)
            throws NotImplementedException {
        log.debug("request for update location id: {} from user:{}", id, userId);
        throw new NotImplementedException("Method update() in AdminLocationController is not implemented");
    }

    /**
     * Получить список локаций, созданных текущим пользователем
     * @return список DTO локаций (во всех статусах)
     */
    // TODO:
    //  1. пагинация
    //  2. фильтрация по статусу, имени, координатам (с неким радиусом)
    @GetMapping
    Collection<LocationDtoOut> getAll(@PathVariable @Min(1) Long userId) throws NotImplementedException {
        log.debug("request for getting all locations of user: {}", userId);
        throw new NotImplementedException("Method getAll() in PrivateLocationController is not implemented");
    }

    /**
     * Удалить существующую неопубликованную локацию от имени создателя.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable @Min(1) Long userId,
                @PathVariable @Min(1) Long id) {
        log.debug("request for delete location id: {} from user:{}", id, userId);
        locationService.delete(id, userId);
    }
}
