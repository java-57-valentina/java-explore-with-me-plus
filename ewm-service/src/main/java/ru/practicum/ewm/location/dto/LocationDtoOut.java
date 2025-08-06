package ru.practicum.ewm.location.dto;

import lombok.*;
import ru.practicum.ewm.location.model.LocationState;
import ru.practicum.ewm.user.dto.UserDtoOut;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LocationDtoOut {
    private Long id;
    private String name;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
    private UserDtoOut creator;
    private LocationState state;
}
