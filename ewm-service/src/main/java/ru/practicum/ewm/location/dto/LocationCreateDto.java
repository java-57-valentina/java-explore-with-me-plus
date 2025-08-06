package ru.practicum.ewm.location.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LocationCreateDto {

    // @NotNull(message = "The location name cannot be empty")
    @NotBlank(message = "The location name cannot be blank")
    private String name;

    private String description;
    private String address;

    @NotNull(message = "The location latitude cannot be empty")
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double latitude;

    @NotNull(message = "The location longitude cannot be empty")
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double longitude;
}
