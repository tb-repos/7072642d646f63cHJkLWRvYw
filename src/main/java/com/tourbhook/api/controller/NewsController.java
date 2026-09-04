package com.tourbhook.api.controller;

import com.tourbhook.api.dto.common.ApiResponse;
import com.tourbhook.api.dto.news.CreateNewsItemRequest;
import com.tourbhook.api.dto.news.NewsItemResponse;
import com.tourbhook.api.dto.news.UpdateNewsItemRequest;
import com.tourbhook.api.service.NewsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NewsItemResponse>>> getFeed(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String category
    ) {
        return ResponseEntity.ok(
                ApiResponse.<List<NewsItemResponse>>builder()
                        .success(true)
                        .message("News feed fetched successfully")
                        .data(newsService.getFeed(region, category))
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NewsItemResponse>> getNewsItem(@PathVariable String id) {
        return ResponseEntity.ok(
                ApiResponse.<NewsItemResponse>builder()
                        .success(true)
                        .message("News item fetched successfully")
                        .data(newsService.getNewsItem(id))
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NewsItemResponse>> createNewsItem(
            @Valid @RequestBody CreateNewsItemRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<NewsItemResponse>builder()
                        .success(true)
                        .message("News item created successfully")
                        .data(newsService.createNewsItem(request))
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NewsItemResponse>> updateNewsItem(
            @PathVariable String id,
            @Valid @RequestBody UpdateNewsItemRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.<NewsItemResponse>builder()
                        .success(true)
                        .message("News item updated successfully")
                        .data(newsService.updateNewsItem(id, request))
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNewsItem(@PathVariable String id) {
        newsService.deleteNewsItem(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("News item deleted successfully")
                        .build()
        );
    }
}