package ru.practicum.ewm.event.model;

import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EventFilter {
    private String text;
    private List<Long> categories;
    private Boolean paid;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;

    @Builder.Default
    private Boolean onlyAvailable = false;

    @Builder.Default
    private String sort = "EVENT_DATE";

    @Builder.Default
    private Integer from = 0;

    @Builder.Default
    private Integer size = 10;

    private Pageable pageable;

    @Builder.Default
    private EventState state = EventState.PUBLISHED;


    public Pageable getPageable() {
        if (pageable == null) {
            Sort sort = Sort.by(Sort.Direction.DESC,
                    this.sort.equals("VIEWS") ? "views" : "eventDate");
            this.pageable = PageRequest.of(from / size, size, sort);
        }
        return pageable;
    }
}