package com.shrinetours.api.repository;

import com.shrinetours.api.entity.Subscription;
import com.shrinetours.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, String> {
    Optional<Subscription> findByUser(User user);
}
