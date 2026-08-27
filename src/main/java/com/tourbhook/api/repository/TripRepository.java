package com.tourbhook.api.repository;

import com.tourbhook.api.entity.Trip;
import com.tourbhook.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripRepository extends JpaRepository<Trip, String> {
    List<Trip> findByUserOrderByStartDateDesc(User user);
}
