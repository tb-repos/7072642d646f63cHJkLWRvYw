package com.shrinetours.api.entity;

import com.shrinetours.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    @Id
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false)
    private String password;

    private LocalDate dob;
    
    private String avatarUrl;

    @Column(nullable = false, length = 50)
    private String level;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal levelProgress;

    @Column(nullable = false)
    private Integer tripsCompleted;

    @Column(nullable = false)
    private boolean premium;

    @Column(nullable = false)
    private boolean deleted;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        if (level == null) level = "Explorer";
        if (levelProgress == null) levelProgress = BigDecimal.ZERO;
        if (tripsCompleted == null) tripsCompleted = 0;
    }
}
