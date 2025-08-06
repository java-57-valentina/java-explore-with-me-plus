package ru.practicum.ewm.location.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.location.dto.LocationCreateDto;
import ru.practicum.ewm.location.dto.LocationDtoOut;
import ru.practicum.ewm.location.dto.LocationFullDtoOut;
import ru.practicum.ewm.location.dto.LocationUpdateUserDto;
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
    @PatchMapping("/{id}")
    LocationFullDtoOut update(@PathVariable @Min(1) Long userId,
                                      @PathVariable @Min(1) Long id,
                                      @RequestBody @Valid LocationUpdateUserDto dto) {
        log.debug("request for update location id: {} from user:{}", id, userId);
        return locationService.update(id, userId, dto);
    }

    /**
     * Получить список локаций, созданных текущим пользователем
     * @return список DTO локаций (во всех статусах)
     */
    // TODO:
    //  1. пагинация
    //  2. фильтрация по статусу, имени, координатам (с неким радиусом)
    @GetMapping
    Collection<LocationFullDtoOut> getAll(@PathVariable @Min(1) Long userId) {
        log.debug("request for getting all locations of user: {}", userId);
        return locationService.findAllByUser(userId);
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
