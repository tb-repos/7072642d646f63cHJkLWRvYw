package com.shrinetours.api.repository;

import com.shrinetours.api.entity.PaymentMethod;
import com.shrinetours.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, String> {
    List<PaymentMethod> findByUserOrderByPrimaryMethodDescCreatedAtDesc(User user);
    Optional<PaymentMethod> findByIdAndUser(String id, User user);
	List<PaymentMethod> findByUser(User user);
}
