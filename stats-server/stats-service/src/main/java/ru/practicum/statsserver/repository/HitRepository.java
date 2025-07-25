package ru.practicum.statsserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.statsserver.model.Hit;
import ru.practicum.statsserver.model.Stats;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface HitRepository extends JpaRepository<Hit, Long> {

    @Query("""
            SELECT Stats(h.service, h.uri, COUNT(DISTINCT h.ip))
            FROM Hit h
            WHERE h.dateTime BETWEEN :start AND :end
            AND (:uris IS NULL OR h.uri IN :uris)
            GROUP BY h.service, h.uri
            ORDER BY COUNT(DISTINCT h.ip) DESC, h.service, h.uri
            """)
    List<Stats> findStatsUnique(@Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end,
                                @Param("uris") Collection<String> uris);

    @Query("""
            SELECT Stats(h.service, h.uri, COUNT(h.ip))
            FROM Hit h
            WHERE h.dateTime BETWEEN :start AND :end
            AND (:uris IS NULL OR h.uri IN :uris)
            GROUP BY h.service, h.uri
            ORDER BY COUNT(h.ip) DESC, h.service, h.uri
            """)
    List<Stats> findStatsAll(@Param("start") LocalDateTime start,
                             @Param("end") LocalDateTime end,
                             @Param("uris") Collection<String> uris);
}
