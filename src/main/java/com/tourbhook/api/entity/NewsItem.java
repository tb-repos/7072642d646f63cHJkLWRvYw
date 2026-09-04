package com.tourbhook.api.entity;

import com.tourbhook.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "news_items", indexes = {
        @Index(name = "idx_news_region", columnList = "region"),
        @Index(name = "idx_news_category", columnList = "category"),
        @Index(name = "idx_news_type", columnList = "type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsItem extends BaseEntity {

    @Id
    private String id;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(length = 2000)
    private String summary;

    private String sourceUrl;

    @Column(nullable = false, length = 100)
    private String region;

    @Column(nullable = false, length = 100)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NewsType type;

    @Column(nullable = false)
    private Instant publishedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        if (publishedAt == null) publishedAt = Instant.now();
    }
}