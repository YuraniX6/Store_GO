package com.storego.MarketplaceService.repository;

import com.storego.MarketplaceService.entity.MarketplacePost;
import com.storego.MarketplaceService.entity.PublicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarketplacePostRepository extends JpaRepository<MarketplacePost, Long> {
    
    /**
     * Busca todas las publicaciones por ID de skin
     */
    Page<MarketplacePost> findBySkinId(String skinId, Pageable pageable);
    
    /**
     * Busca todas las publicaciones de un usuario
     */
    Page<MarketplacePost> findByOwnerId(Long ownerId, Pageable pageable);
    
    /**
     * Busca publicaciones por estado
     */
    Page<MarketplacePost> findByPublicacionEstado(PublicationStatus status, Pageable pageable);
    
    /**
     * Busca publicaciones activas
     */
    Page<MarketplacePost> findByPublicacionEstadoAndOwnerId(PublicationStatus status, Long ownerId, Pageable pageable);
    
    /**
     * Busca una publicación específica por ID del propietario y ID de la skin
     */
    Optional<MarketplacePost> findByOwnerIdAndSkinIdAndPublicacionEstado(Long ownerId, String skinId, PublicationStatus status);
    
    /**
     * Obtiene todas las publicaciones activas de un usuario
     */
    List<MarketplacePost> findAllByOwnerIdAndPublicacionEstado(Long ownerId, PublicationStatus status);
    
    /**
     * Obtiene todas las publicaciones activas de una skin
     */
    List<MarketplacePost> findAllBySkinIdAndPublicacionEstado(String skinId, PublicationStatus status);
}
