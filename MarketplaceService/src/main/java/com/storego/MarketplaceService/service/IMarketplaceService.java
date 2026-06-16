package com.storego.MarketplaceService.service;

import com.storego.MarketplaceService.dto.MarketplaceRequestDTO;
import com.storego.MarketplaceService.dto.MarketplaceResponseDTO;
import com.storego.MarketplaceService.entity.PublicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface IMarketplaceService {
    
    /**
     * Crea una nueva publicación de marketplace
     */
    MarketplaceResponseDTO createPost(Long ownerId, MarketplaceRequestDTO requestDTO, String token);
    
    /**
     * Obtiene todas las publicaciones con paginación
     */
    Page<MarketplaceResponseDTO> getAllPosts(Pageable pageable);
    
    /**
     * Obtiene una publicación por ID
     */
    Optional<MarketplaceResponseDTO> getPostById(Long id);
    
    /**
     * Obtiene todas las publicaciones de un usuario
     */
    Page<MarketplaceResponseDTO> getPostsByOwnerId(Long ownerId, Pageable pageable);
    
    /**
     * Obtiene todas las publicaciones de una skin
     */
    Page<MarketplaceResponseDTO> getPostsBySkinId(String skinId, Pageable pageable);
    
    /**
     * Obtiene todas las publicaciones activas
     */
    Page<MarketplaceResponseDTO> getActivePosts(Pageable pageable);
    
    /**
     * Elimina una publicación
     */
    void deletePost(Long id, Long ownerId);
    
    /**
     * Actualiza el estado de una publicación
     */
    MarketplaceResponseDTO updatePostStatus(Long id, PublicationStatus status, Long ownerId);
    
    /**
     * Obtiene todas las publicaciones de un usuario por estado
     */
    List<MarketplaceResponseDTO> getUserPostsByStatus(Long ownerId, PublicationStatus status);
    
    /**
     * Obtiene todas las publicaciones activas de una skin
     */
    List<MarketplaceResponseDTO> getActiveSkinPosts(String skinId);
}
