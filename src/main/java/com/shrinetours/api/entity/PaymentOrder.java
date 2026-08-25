package com.shrinetours.api.entity;

import com.shrinetours.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_orders", indexes = {
        @Index(name = "idx_payment_orders_user", columnList = "user_id"),
        @Index(name = "idx_payment_orders_razorpay_order", columnList = "razorpayOrderId", unique = true),
        @Index(name = "idx_payment_orders_payment_id", columnList = "razorpayPaymentId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentOrder extends BaseEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 40)
    private String planCode;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(nullable = false, unique = true, length = 80)
    private String razorpayOrderId;

    @Column(length = 80)
    private String razorpayPaymentId;

    @Column(length = 255)
    private String razorpaySignature;

    @Column(nullable = false, unique = true, length = 80)
    private String receipt;

    @Column(length = 500)
    private String notesJson;

    private Instant paidAt;

    @Column(length = 1000)
    private String gatewayResponse;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        if (status == null || status.isBlank()) status = "CREATED";
    }
}
