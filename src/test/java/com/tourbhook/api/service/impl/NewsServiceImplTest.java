package com.tourbhook.api.service.impl;

import com.tourbhook.api.dto.news.CreateNewsItemRequest;
import com.tourbhook.api.dto.news.NewsItemResponse;
import com.tourbhook.api.dto.news.UpdateNewsItemRequest;
import com.tourbhook.api.entity.NewsItem;
import com.tourbhook.api.entity.NewsType;
import com.tourbhook.api.repository.NewsItemRepository;
import com.tourbhook.api.repository.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsServiceImplTest {

    @Mock
    private NewsItemRepository newsItemRepository;

    @InjectMocks
    private NewsServiceImpl newsService;

    private NewsItem item(String id, NewsType type, String region, String category, Instant publishedAt) {
        return NewsItem.builder()
                .id(id)
                .title(id + " title")
                .region(region)
                .category(category)
                .type(type)
                .publishedAt(publishedAt)
                .build();
    }

    @Test
    void getFeed_ordersAdvisoriesBeforeGeneralNews_regardlessOfPublishDate() {
        Instant now = Instant.now();
        NewsItem oldAdvisory = item("advisory-old", NewsType.ADVISORY, "IN", "safety", now.minus(10, ChronoUnit.DAYS));
        NewsItem freshGeneral = item("general-fresh", NewsType.GENERAL, "IN", "culture", now);

        when(newsItemRepository.findAll()).thenReturn(List.of(freshGeneral, oldAdvisory));

        List<NewsItemResponse> feed = newsService.getFeed(null, null);

        assertThat(feed).extracting(NewsItemResponse::id)
                .containsExactly("advisory-old", "general-fresh");
    }

    @Test
    void getFeed_withinSameType_ordersByMostRecentFirst() {
        Instant now = Instant.now();
        NewsItem older = item("older", NewsType.GENERAL, "IN", "culture", now.minus(5, ChronoUnit.DAYS));
        NewsItem newer = item("newer", NewsType.GENERAL, "IN", "culture", now);

        when(newsItemRepository.findAll()).thenReturn(List.of(older, newer));

        List<NewsItemResponse> feed = newsService.getFeed(null, null);

        assertThat(feed).extracting(NewsItemResponse::id).containsExactly("newer", "older");
    }

    @Test
    void getFeed_filtersByRegionAndCategoryWhenBothProvided() {
        NewsItem match = item("match", NewsType.GENERAL, "IN", "weather", Instant.now());
        when(newsItemRepository.findByRegionIgnoreCaseAndCategoryIgnoreCase("IN", "weather"))
                .thenReturn(List.of(match));

        List<NewsItemResponse> feed = newsService.getFeed("IN", "weather");

        assertThat(feed).extracting(NewsItemResponse::id).containsExactly("match");
        verify(newsItemRepository, never()).findAll();
    }

    @Test
    void getFeed_filtersByRegionOnly() {
        when(newsItemRepository.findByRegionIgnoreCase("IN")).thenReturn(List.of());

        newsService.getFeed("IN", null);

        verify(newsItemRepository).findByRegionIgnoreCase("IN");
        verify(newsItemRepository, never()).findAll();
    }

    @Test
    void getFeed_filtersByCategoryOnly() {
        when(newsItemRepository.findByCategoryIgnoreCase("safety")).thenReturn(List.of());

        newsService.getFeed(null, "safety");

        verify(newsItemRepository).findByCategoryIgnoreCase("safety");
        verify(newsItemRepository, never()).findAll();
    }

    @Test
    void getNewsItem_whenMissing_throwsResourceNotFound() {
        when(newsItemRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newsService.getNewsItem("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createNewsItem_savesWithProvidedFields() {
        CreateNewsItemRequest request = new CreateNewsItemRequest(
                "Flight disruptions in Chennai", "Heavy rain causing delays", "https://example.com/article",
                "IN", "weather", NewsType.ADVISORY, null);

        ArgumentCaptor<NewsItem> captor = ArgumentCaptor.forClass(NewsItem.class);
        when(newsItemRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        NewsItemResponse response = newsService.createNewsItem(request);

        assertThat(captor.getValue().getTitle()).isEqualTo("Flight disruptions in Chennai");
        assertThat(captor.getValue().getType()).isEqualTo(NewsType.ADVISORY);
        assertThat(captor.getValue().getPublishedAt()).isNotNull();
        assertThat(response.region()).isEqualTo("IN");
    }

    @Test
    void updateNewsItem_updatesOnlySuppliedFields() {
        NewsItem existing = item("news-1", NewsType.GENERAL, "IN", "culture", Instant.now());
        existing.setTitle("Old title");
        when(newsItemRepository.findById("news-1")).thenReturn(Optional.of(existing));
        when(newsItemRepository.save(any(NewsItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateNewsItemRequest request = new UpdateNewsItemRequest("New title", null, null, null, null, null);
        NewsItemResponse response = newsService.updateNewsItem("news-1", request);

        assertThat(response.title()).isEqualTo("New title");
        assertThat(response.region()).isEqualTo("IN"); // untouched
    }

    @Test
    void updateNewsItem_whenMissing_throwsResourceNotFound() {
        when(newsItemRepository.findById("missing")).thenReturn(Optional.empty());
        UpdateNewsItemRequest request = new UpdateNewsItemRequest("x", null, null, null, null, null);

        assertThatThrownBy(() -> newsService.updateNewsItem("missing", request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteNewsItem_whenExists_deletes() {
        when(newsItemRepository.existsById("news-1")).thenReturn(true);

        newsService.deleteNewsItem("news-1");

        verify(newsItemRepository).deleteById("news-1");
    }

    @Test
    void deleteNewsItem_whenMissing_throwsResourceNotFound() {
        when(newsItemRepository.existsById("missing")).thenReturn(false);

        assertThatThrownBy(() -> newsService.deleteNewsItem("missing"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(newsItemRepository, never()).deleteById(any());
    }
}