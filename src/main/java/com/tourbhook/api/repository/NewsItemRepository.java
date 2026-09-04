package com.tourbhook.api.repository;

import com.tourbhook.api.entity.NewsItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsItemRepository extends JpaRepository<NewsItem, String> {

    List<NewsItem> findByRegionIgnoreCase(String region);

    List<NewsItem> findByCategoryIgnoreCase(String category);

    List<NewsItem> findByRegionIgnoreCaseAndCategoryIgnoreCase(String region, String category);
}