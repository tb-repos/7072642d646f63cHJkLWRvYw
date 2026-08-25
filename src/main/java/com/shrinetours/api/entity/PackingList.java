 package com.shrinetours.api.entity;

import com.shrinetours.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "packing_lists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackingList extends BaseEntity {
    @Id
    private String id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false, unique = true)
    private Trip trip;

    @ElementCollection
    @CollectionTable(name = "packing_list_transports", joinColumns = @JoinColumn(name = "packing_list_id"))
    @Column(name = "transport_name")
    @Builder.Default
    private List<String> selectedTransports = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "packing_list_id")
    @Builder.Default
    private List<PackingCategory> categories = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (id == null) 
        	id = UUID.randomUUID().toString();
    }
}
