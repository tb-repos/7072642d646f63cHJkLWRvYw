package com.tourbhook.api.repository;

import com.tourbhook.api.entity.PackingList;
import com.tourbhook.api.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PackingListRepository extends JpaRepository<PackingList, String> {
    Optional<PackingList> findByTrip(Trip trip);
}
