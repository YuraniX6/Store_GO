package com.storego.MarketplaceService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "marketplace_posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketplacePost {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "skin_id", nullable = false)
    private String skinId;

    @Column(nullable = false, length = 150)
    private String nombreSkin;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private Double precio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PublicationStatus publicacionEstado;

    @Column(nullable = false)
    private LocalDateTime publicacionFecha;

    @Column(nullable = false)
    private String nombreUsuario;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        publicacionFecha = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
