package com.tourbhook.api.service.impl;

import com.tourbhook.api.dto.news.CreateNewsItemRequest;
import com.tourbhook.api.dto.news.NewsItemResponse;
import com.tourbhook.api.dto.news.UpdateNewsItemRequest;
import com.tourbhook.api.entity.NewsItem;
import com.tourbhook.api.entity.NewsType;
import com.tourbhook.api.repository.NewsItemRepository;
import com.tourbhook.api.repository.exception.ResourceNotFoundException;
import com.tourbhook.api.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NewsServiceImpl implements NewsService {

    private static final Comparator<NewsItem> FEED_ORDER = Comparator
            .comparing((NewsItem item) -> item.getType() == NewsType.ADVISORY ? 0 : 1)
            .thenComparing(NewsItem::getPublishedAt, Comparator.reverseOrder());

    private final NewsItemRepository newsItemRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NewsItemResponse> getFeed(String region, String category) {
        List<NewsItem> items = fetchByFilters(region, category);
        return items.stream()
                .sorted(FEED_ORDER)
                .map(this::mapNewsItem)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NewsItemResponse getNewsItem(String id) {
        return newsItemRepository.findById(id)
                .map(this::mapNewsItem)
                .orElseThrow(() -> new ResourceNotFoundException("News item not found"));
    }

    @Override
    public NewsItemResponse createNewsItem(CreateNewsItemRequest request) {
        NewsItem item = NewsItem.builder()
                .title(request.title().trim())
                .summary(normalize(request.summary()))
                .sourceUrl(normalize(request.sourceUrl()))
                .region(request.region().trim())
                .category(request.category().trim())
                .type(request.type())
                .publishedAt(request.publishedAt() != null ? request.publishedAt() : Instant.now())
                .build();

        NewsItem saved = newsItemRepository.save(item);
        log.info("Created news item {} ({}, region={})", saved.getId(), saved.getType(), saved.getRegion());
        return mapNewsItem(saved);
    }

    @Override
    public NewsItemResponse updateNewsItem(String id, UpdateNewsItemRequest request) {
        NewsItem item = newsItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News item not found"));

        if (request.title() != null) {
            item.setTitle(request.title().trim());
        }
        if (request.summary() != null) {
            item.setSummary(normalize(request.summary()));
        }
        if (request.sourceUrl() != null) {
            item.setSourceUrl(normalize(request.sourceUrl()));
        }
        if (request.region() != null) {
            item.setRegion(request.region().trim());
        }
        if (request.category() != null) {
            item.setCategory(request.category().trim());
        }
        if (request.type() != null) {
            item.setType(request.type());
        }

        return mapNewsItem(newsItemRepository.save(item));
    }

    @Override
    public void deleteNewsItem(String id) {
        if (!newsItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("News item not found");
        }
        newsItemRepository.deleteById(id);
    }

    private List<NewsItem> fetchByFilters(String region, String category) {
        boolean hasRegion = region != null && !region.isBlank();
        boolean hasCategory = category != null && !category.isBlank();

        if (hasRegion && hasCategory) {
            return newsItemRepository.findByRegionIgnoreCaseAndCategoryIgnoreCase(region.trim(), category.trim());
        }
        if (hasRegion) {
            return newsItemRepository.findByRegionIgnoreCase(region.trim());
        }
        if (hasCategory) {
            return newsItemRepository.findByCategoryIgnoreCase(category.trim());
        }
        return newsItemRepository.findAll();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private NewsItemResponse mapNewsItem(NewsItem item) {
        return new NewsItemResponse(
                item.getId(),
                item.getTitle(),
                item.getSummary(),
                item.getSourceUrl(),
                item.getRegion(),
                item.getCategory(),
                item.getType(),
                item.getPublishedAt()
        );
    }
}