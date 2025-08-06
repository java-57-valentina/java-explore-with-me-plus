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
@RequestMapping("/admin/locations")
public class AdminLocationController {

    private final LocationService locationService;

    /**
     * Создать локацию от имени администратора.
     * Локация автоматически переводится в статус APPROVED
     * @return DTO созданной локаций
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    LocationDtoOut create(@RequestBody @Valid LocationCreateDto dto) {
        log.debug("request for adding location from admin: {}", dto);
        return locationService.addLocationByAdmin(dto);
    }

    /**
     * Редактировать существующую локацию от имени администратора.
     * Локация автоматически переводится в статус APPROVED (или REJECTED, если админ явно указал)
     * @return DTO обновленной локаций
     */
    // TODO:
    @PatchMapping("/{id}")
    Collection<LocationDtoOut> update(@PathVariable @Min(1) Long id)
            throws NotImplementedException {
        log.debug("request for update location id: {} from admin", id);
        throw new NotImplementedException("Method update() in AdminLocationController is not implemented");
    }

    /**
     * Получить список локаций от имени администратора.
     * @return список DTO локаций (во всех статусах)
     */
    // TODO:
    //  1. пагинация
    //  2. фильтрация по статусу, создателям, количеству мероприятий, имени, координатам (с неким радиусом)
    @GetMapping
    Collection<LocationDtoOut> getAll() {
        log.debug("request for getting all locations from admin");
        return locationService.findAll();
    }

    /**
     * Удалить существующую локацию от имени администратора.
     * Удаляется только локация, не имеющая мероприятий
     */
    // TODO:
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable @Min(1) Long id) {
        log.info("request for delete location id:{} from admin", id);
        locationService.delete(id);
    }


    /**
     * (Для отладки, удалить перед сдачей проекта)
     * Возвращает расстояние в метрах между координатами
     */
    // TODO:
    @GetMapping("distance/{id1}/{id2}")
    Double getDistance(@PathVariable @Min(1) Long id1, @PathVariable @Min(1) Long id2) {
        log.info("request for calcutale distance between locations id:{} and id:{}", id1, id2);
        return locationService.getDistance(id1, id2);
    }
}
