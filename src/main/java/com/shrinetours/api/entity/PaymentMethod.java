package com.shrinetours.api.entity;

import com.shrinetours.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "payment_methods", indexes = @Index(name = "idx_payment_methods_user", columnList = "user_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethod extends BaseEntity {
    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, length = 4)
    private String lastFour;

    @Column(nullable = false)
    private String holderName;

    @Column(nullable = false)
    private String expiry;

    @Column(nullable = false)
    private Boolean primaryMethod;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        if (primaryMethod == null) primaryMethod = false;
    }
}
