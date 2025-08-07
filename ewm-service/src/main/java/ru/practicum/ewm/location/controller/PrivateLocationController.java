package ru.practicum.ewm.location.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.location.dto.*;
import ru.practicum.ewm.location.model.LocationState;
import ru.practicum.ewm.location.model.LocationPrivateFilter;
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
    LocationPrivateDtoOut create(@PathVariable @Min(1) Long userId,
                                 @RequestBody @Valid LocationCreateDto dto) {
        log.debug("request for adding location {} from user: {}", dto, userId);
        return locationService.addLocation(userId, dto);
    }

    /**
     * Редактировать существующую локацию от имени создателя (если локация в статусе PENDING)
     * @return DTO обновленной локации
     */
    @PatchMapping("/{id}")
    LocationPrivateDtoOut update(
            @PathVariable @Min(1) Long userId,
            @PathVariable @Min(1) Long id,
            @RequestBody @Valid LocationUpdateUserDto dto) {
        log.debug("request for update location id: {} from user:{}", id, userId);
        return locationService.update(id, userId, dto);
    }

    /**
     * Получить список локаций, созданных текущим пользователем
     * Фильтрация по статусу, имени, координатам (с неким радиусом)
     * @return список DTO локаций
     */
    @GetMapping
    Collection<LocationPrivateDtoOut> getAll(
            @PathVariable @Min(1) Long userId,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) LocationState state,
            @RequestParam(required = false) @DecimalMin("-90.0")  @DecimalMax("90.0")  Double lat,
            @RequestParam(required = false) @DecimalMin("-180.0") @DecimalMax("180.0") Double lon,
            @RequestParam(defaultValue = "10") Double radius,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "10") Integer limit) {
        log.debug("request for getting all locations of user: {}", userId);
        LocationPrivateFilter filter = LocationPrivateFilter.builder()
                .text(text)
                .creator(userId)
                .state(state)
                .lat(lat)
                .lon(lon)
                .radius(radius)
                .offset(offset)
                .limit(limit)
                .build();
        return locationService.findAllByFilter(userId, filter);
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
