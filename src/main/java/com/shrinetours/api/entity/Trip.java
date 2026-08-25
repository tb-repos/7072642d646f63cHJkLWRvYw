package com.shrinetours.api.entity;

import com.shrinetours.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "trips")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip extends BaseEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 120)
    private String city;
    @Column(name = "imageUrl", length = 2000)
    private String imageUrl;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Integer placesCount;

    @Column(nullable = false)
    private Integer days;

    @Column(nullable = false)
    private Integer adults;

    @Column(nullable = false)
    private Integer kids;

    @Column(nullable = false, length = 40)
    private String tripStyle;

    private String purposeofTravel;

    @Column(name = "starting_point_id")
    private String startingPointId;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TripPlace> tripPlaces = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        if (placesCount == null) placesCount = 0;
        if (days == null) days = 1;
        if (adults == null) adults = 1;
        if (kids == null) kids = 0;
        if (tripStyle == null || tripStyle.isBlank()) tripStyle = "balanced";
        if (purposeofTravel == null || purposeofTravel.isBlank()) purposeofTravel = "general";
    }
}