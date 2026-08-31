package com.lucas.landmarketplace.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.locationtech.jts.geom.Polygon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "land_plots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LandPlot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "boundary", nullable = false, columnDefinition = "geometry(Polygon,4326)")
    private Polygon boundary;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, length = 200)
    private String contact;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
