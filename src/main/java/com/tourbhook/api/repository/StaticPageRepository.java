package com.tourbhook.api.repository;

import com.tourbhook.api.entity.StaticPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaticPageRepository extends JpaRepository<StaticPage, String> {
    Optional<StaticPage> findBySlug(String slug);
}
