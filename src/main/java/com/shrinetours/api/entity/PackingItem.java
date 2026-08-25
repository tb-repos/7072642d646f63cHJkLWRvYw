package com.shrinetours.api.entity;

import com.shrinetours.api.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "packing_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackingItem extends BaseEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Boolean checked;

    @Column(nullable = false)
    private Integer quantity;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        if (checked == null) checked = false;
        if (quantity == null) quantity = 1;
    }
}
