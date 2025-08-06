package ru.practicum.ewm.location.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.location.dto.LocationDtoOut;
import ru.practicum.ewm.location.service.LocationService;

import java.util.Collection;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/locations")
public class PublicLocationController {

    private final LocationService locationService;

    /**
     * Получить список APPROVED локаций
     * @return список DTO локаций
     */
    // TODO:
    //  1. пагинация
    //  2. фильтрация по статусу, имени, координатам (с неким радиусом)
    @GetMapping
    Collection<LocationDtoOut> getAll() {
        log.debug("request for getting approved locations");
        return locationService.findAllApproved();
    }
}
