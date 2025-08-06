package ru.practicum.ewm.location.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.ewm.user.model.User;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    User creator;

    @Column(nullable = false)
    String name;

    String address;

    @Column(nullable = false)
    Double latitude;

    @Column(nullable = false)
    Double longitude;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    LocationState state = LocationState.PENDING;
}
