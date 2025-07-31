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
public class EventAdminFilter {

    private List<Long> users;
    private List<Long> categories;
    private List<EventState> states;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;

    @Builder.Default
    private Integer from = 0;

    @Builder.Default
    private Integer size = 10;

    private Pageable pageable;

    public Pageable getPageable() {
        if (pageable == null) {
            Sort sort = Sort.by(Sort.Direction.DESC, "id");
            this.pageable = PageRequest.of(from / size, size, sort);
        }
        return pageable;
    }
}
