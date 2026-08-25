package com.shrinetours.api.repository;

import com.shrinetours.api.entity.Trip;
import com.shrinetours.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripRepository extends JpaRepository<Trip, String> {
    List<Trip> findByUserOrderByStartDateDesc(User user);
}
