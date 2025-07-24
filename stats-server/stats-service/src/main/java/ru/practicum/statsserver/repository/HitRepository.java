package ru.practicum.statsserver.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.practicum.statsserver.model.Hit;
import ru.practicum.statsserver.model.Stats;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository("hitRepository")
@RequiredArgsConstructor
public class HitRepository  {

    protected final JdbcTemplate jdbcTemplate;
    protected final RowMapper<Stats> rowMapper;

    private static final String SQL_INSERT = """
            INSERT INTO hits (service, uri, ip, timestamp)
            VALUES (?, ?, ?, ?);
            """;

    private static final String SQL_SELECT = """
                SELECT service, uri, %s as hits_count
                FROM hits
                WHERE timestamp BETWEEN ? AND ?
                AND uri IN (%s)
                GROUP BY service, uri
                ORDER BY service, uri
                """;

    public void save(Hit hit) {
        jdbcTemplate.update(SQL_INSERT,
                hit.getService(),
                hit.getUri(),
                hit.getIp(),
                hit.getDateTime()
        );
    }

    public Collection<Stats> getStatistics(LocalDateTime start,
                                           LocalDateTime end,
                                           Collection<String> uris,
                                           boolean unique) {

        String placeholders = uris.stream()
                .map(uri -> "?")
                .collect(Collectors.joining(", "));

        String query = SQL_SELECT.formatted(
                unique ? "COUNT(DISTINCT ip)" : "COUNT(ip)",
                placeholders
        );

        Object[] params = Stream.concat(
                Stream.of(start, end),
                uris.stream()).toArray();

        return jdbcTemplate.query(query, rowMapper, params);
    }
}