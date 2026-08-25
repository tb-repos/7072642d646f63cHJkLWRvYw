//package com.shrinetours.api.entity;
//
//import java.math.BigDecimal;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.Table;
//import jakarta.persistence.UniqueConstraint;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//
//@Entity
//@Table(
//        name = "places",
//        uniqueConstraints = {
//                @UniqueConstraint(name = "uk_places_google_place_id", columnNames = "google_place_id")
//        }
//)
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Place {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    private String id;
//
//    @Column(name = "google_place_id", unique = true, length = 255)
//    private String googlePlaceId;
//
//    @Column(nullable = false)
//    private String name;
//
//    private String city;
//
//    private String category;
//
//    @Column(length = 1000)
//    private String address;
//
//    @Column(length = 2000)
//    private String imageUrl;
//
//    private Double latitude;
//    private Double longitude;
//
//    private String typicalDuration;
//
//    @Column(precision = 3, scale = 2)
//    private BigDecimal rating;
//
//    private Integer reviewsCount;
//
//    private boolean verified;
//
//    private boolean suggested;
//
//    @Column(length = 500)
//    private String sourceQuery;
//
//	
//}
package com.shrinetours.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "places")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Place {

    @Id
    private String id;

    @Column(name = "google_place_id", unique = true)
    private String googlePlaceId;

    @Column(nullable = false)
    private String name;

    private String city;

    private String category;

    @Column(length = 1000)
    private String address;

    @Column(name = "image_url", length = 2000)
    private String imageUrl;

    private Double latitude;

    private Double longitude;

    @Column(name = "typical_duration")
    private String typicalDuration;

    private BigDecimal rating;

    @Column(name = "reviews_count")
    private Integer reviewsCount;

    private boolean verified;

    private boolean suggested;

    @Column(name = "source_query")
    private String sourceQuery;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }

        if (this.rating == null) {
            this.rating = BigDecimal.ZERO;
        }

        if (this.reviewsCount == null) {
            this.reviewsCount = 0;
        }

        if (this.typicalDuration == null || this.typicalDuration.isBlank()) {
            this.typicalDuration = "2-3 hours";
        }
    }
}