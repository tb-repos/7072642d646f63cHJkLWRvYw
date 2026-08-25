package com.shrinetours.api.repository;

import com.shrinetours.api.entity.PaymentOrder;
import com.shrinetours.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, String> {
    Optional<PaymentOrder> findByRazorpayOrderId(String razorpayOrderId);
    Optional<PaymentOrder> findByRazorpayPaymentId(String razorpayPaymentId);
    List<PaymentOrder> findByUserOrderByCreatedAtDesc(User user);
}
