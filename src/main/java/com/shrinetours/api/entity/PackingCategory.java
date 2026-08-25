package com.shrinetours.api.entity;

import com.shrinetours.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "packing_categories", indexes = @Index(name = "idx_packing_categories_template", columnList = "templateOnly"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackingCategory extends BaseEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    private String icon;

    @Column(nullable = false)
    private Boolean templateOnly;

    @Column(nullable = false)
    private Boolean checked;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "packing_category_id")
    @Builder.Default
    private List<PackingItem> items = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        if (templateOnly == null) templateOnly = false;
        if (checked == null) checked = false;
    }
}
