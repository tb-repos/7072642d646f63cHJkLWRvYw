package com.tourbhook.api.repository;

import com.tourbhook.api.entity.PackingCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PackingCategoryRepository extends JpaRepository<PackingCategory, String> {
    List<PackingCategory> findByTemplateOnlyTrue();
}
