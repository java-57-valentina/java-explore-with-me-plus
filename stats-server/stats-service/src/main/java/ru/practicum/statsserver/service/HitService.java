package ru.practicum.statsserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.statsdto.HitDto;
import ru.practicum.statsdto.StatsDtoOut;
import ru.practicum.statsserver.exception.ParameterInvalidException;
import ru.practicum.statsserver.mappers.HitMapper;
import ru.practicum.statsserver.mappers.StatsMapper;
import ru.practicum.statsserver.model.Stats;
import ru.practicum.statsserver.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HitService {
    private final HitRepository repository;
    private final HitMapper hitMapper;
    private final StatsMapper statsMapper;

    public void add(HitDto hitDto) {
        repository.save(hitMapper.toHit(hitDto));
    }

    public List<StatsDtoOut> getStatistics(LocalDateTime start,
                                           LocalDateTime end,
                                           List<String> uris,
                                           Boolean unique) {
        if (start.isAfter(end)) {
            throw new ParameterInvalidException("'start' date must be before the 'end' date");
        }

        List<String> safeUris = (uris == null || uris.isEmpty()) ? null : uris;

        List<Stats> stats;

        if (Boolean.TRUE.equals(unique)) {
            stats = repository.findStatsUnique(start, end, safeUris);
        } else {
            stats = repository.findStatsAll(start, end, safeUris);
        }

        return stats.stream()
                .map(statsMapper::toDto)
                .toList();
    }
}