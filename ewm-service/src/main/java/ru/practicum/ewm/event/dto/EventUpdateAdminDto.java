package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

import static ru.practicum.ewm.constants.Constants.DATE_TIME_FORMAT;

@Getter
@Setter
@ToString
public class EventUpdateAdminDto {

    private String title;
    private String annotation;
    private String description;

    @JsonProperty("category")
    private Long categoryId;

    @Future(message = "The event date must be in future")
    @JsonFormat(pattern = DATE_TIME_FORMAT)
    private LocalDateTime eventDate;

    private LocationDto location;

    private Boolean paid;

    @Min(0)
    private Integer participantLimit;
    private Boolean requestModeration;

    private StateAction stateAction;

    public enum StateAction {
        PUBLISH_EVENT,
        REJECT_EVENT
    }
}
