package com.tourbhook.api.repository;

import com.tourbhook.api.entity.Subscription;
import com.tourbhook.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, String> {
    Optional<Subscription> findByUser(User user);
}
