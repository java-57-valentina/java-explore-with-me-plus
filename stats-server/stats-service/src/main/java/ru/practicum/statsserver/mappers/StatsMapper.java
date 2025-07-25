package ru.practicum.statsserver.mappers;

import org.mapstruct.Mapper;
import ru.practicum.statsdto.StatsDtoOut;
import ru.practicum.statsserver.model.Stats;

@Mapper(componentModel = "spring")
public interface StatsMapper {

    StatsDtoOut toDto(Stats stats);

    Stats toStats(StatsDtoOut dto);
}
