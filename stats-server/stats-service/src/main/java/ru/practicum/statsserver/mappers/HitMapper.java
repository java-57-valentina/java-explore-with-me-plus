package ru.practicum.statsserver.mappers;

import org.mapstruct.Mapper;
import ru.practicum.statsdto.HitDto;
import ru.practicum.statsserver.model.Hit;

@Mapper(componentModel = "spring")
public interface HitMapper {

    Hit toHit(HitDto dto);

    HitDto toDto(Hit hit);
}
