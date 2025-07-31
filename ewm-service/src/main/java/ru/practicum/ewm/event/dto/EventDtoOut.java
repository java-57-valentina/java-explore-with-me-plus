package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.ewm.category.dto.CategoryDtoOut;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.user.dto.UserDtoOut;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EventDtoOut {

    private Long id;
    private String title;
    private String annotation;
    private String description;
    private CategoryDtoOut category;
    private UserDtoOut initiator;
    private LocationDto location;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;

    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private EventState state;
    private Integer confirmedRequests;

    @Builder.Default
    private Integer views = 0;
}