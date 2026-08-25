package com.shrinetours.api.repository;

import com.shrinetours.api.entity.PackingList;
import com.shrinetours.api.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PackingListRepository extends JpaRepository<PackingList, String> {
    Optional<PackingList> findByTrip(Trip trip);
}
