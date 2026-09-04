package com.tourbhook.api.service;

import com.tourbhook.api.dto.news.CreateNewsItemRequest;
import com.tourbhook.api.dto.news.NewsItemResponse;
import com.tourbhook.api.dto.news.UpdateNewsItemRequest;
import java.util.List;

public interface NewsService {

    List<NewsItemResponse> getFeed(String region, String category);

    NewsItemResponse getNewsItem(String id);

    NewsItemResponse createNewsItem(CreateNewsItemRequest request);

    NewsItemResponse updateNewsItem(String id, UpdateNewsItemRequest request);

    void deleteNewsItem(String id);
}