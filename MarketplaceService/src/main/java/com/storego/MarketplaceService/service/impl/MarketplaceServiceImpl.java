package com.storego.MarketplaceService.service.impl;

import com.storego.MarketplaceService.client.InventoryClient;
import com.storego.MarketplaceService.dto.MarketplaceRequestDTO;
import com.storego.MarketplaceService.dto.MarketplaceResponseDTO;
import com.storego.MarketplaceService.entity.MarketplacePost;
import com.storego.MarketplaceService.entity.PublicationStatus;
import com.storego.MarketplaceService.exception.InventoryValidationException;
import com.storego.MarketplaceService.exception.MarketplaceNotFoundException;
import com.storego.MarketplaceService.exception.UnauthorizedException;
import com.storego.MarketplaceService.repository.MarketplacePostRepository;
import com.storego.MarketplaceService.security.JwtUtil;
import com.storego.MarketplaceService.service.IMarketplaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MarketplaceServiceImpl implements IMarketplaceService {

    @Autowired
    private MarketplacePostRepository repository;

    @Autowired
    private InventoryClient inventoryClient;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    @Transactional
    public MarketplaceResponseDTO createPost(Long ownerId, MarketplaceRequestDTO requestDTO, String token) {
        log.info("Creando nueva publicación para usuario: {}", ownerId);

        try {
            // Validar propiedad de la skin en InventoryService
            boolean isOwner = inventoryClient.validateSkinOwnership(ownerId, requestDTO.getSkinId(), token);
            
            if (!isOwner) {
                log.warn("Usuario {} no posee la skin {}", ownerId, requestDTO.getSkinId());
                throw new InventoryValidationException(
                        "El usuario no posee la skin especificada. No se puede crear la publicación."
                );
            }

            // Crear la publicación
            MarketplacePost post = MarketplacePost.builder()
                    .ownerId(ownerId)
                    .skinId(requestDTO.getSkinId())
                    .nombreSkin(requestDTO.getNombreSkin())
                    .descripcion(requestDTO.getDescripcion())
                    .precio(requestDTO.getPrecio())
                    .publicacionEstado(PublicationStatus.ACTIVE)
                    .publicacionFecha(LocalDateTime.now())
                    .nombreUsuario(requestDTO.getNombreUsuario())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            MarketplacePost savedPost = repository.save(post);
            log.info("Publicación creada exitosamente con ID: {}", savedPost.getId());

            return mapToResponseDTO(savedPost);
        } catch (InventoryValidationException ex) {
            log.error("Error de validación de inventario: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Error creando publicación: {}", ex.getMessage(), ex);
            throw new RuntimeException("Error creando publicación: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Page<MarketplaceResponseDTO> getAllPosts(Pageable pageable) {
        log.debug("Obteniendo todas las publicaciones con paginación: {}", pageable);
        return repository.findAll(pageable)
                .map(this::mapToResponseDTO);
    }

    @Override
    public Optional<MarketplaceResponseDTO> getPostById(Long id) {
        log.debug("Obteniendo publicación por ID: {}", id);
        return repository.findById(id)
                .map(this::mapToResponseDTO)
                .or(() -> {
                    log.warn("Publicación no encontrada con ID: {}", id);
                    return Optional.empty();
                });
    }

    @Override
    public Page<MarketplaceResponseDTO> getPostsByOwnerId(Long ownerId, Pageable pageable) {
        log.debug("Obteniendo publicaciones del usuario: {}", ownerId);
        return repository.findByOwnerId(ownerId, pageable)
                .map(this::mapToResponseDTO);
    }

    @Override
    public Page<MarketplaceResponseDTO> getPostsBySkinId(String skinId, Pageable pageable) {
        log.debug("Obteniendo publicaciones de la skin: {}", skinId);
        return repository.findBySkinId(skinId, pageable)
                .map(this::mapToResponseDTO);
    }

    @Override
    public Page<MarketplaceResponseDTO> getActivePosts(Pageable pageable) {
        log.debug("Obteniendo publicaciones activas");
        return repository.findByPublicacionEstado(PublicationStatus.ACTIVE, pageable)
                .map(this::mapToResponseDTO);
    }

    @Override
    @Transactional
    public void deletePost(Long id, Long ownerId) {
        log.info("Eliminando publicación ID: {} del usuario: {}", id, ownerId);

        MarketplacePost post = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Publicación no encontrada: {}", id);
                    return new MarketplaceNotFoundException("Publicación no encontrada con ID: " + id);
                });

        // Verificar que el usuario es el propietario
        if (!post.getOwnerId().equals(ownerId)) {
            log.warn("Usuario {} intentó eliminar publicación del usuario {}", ownerId, post.getOwnerId());
            throw new UnauthorizedException("No tienes permiso para eliminar esta publicación");
        }

        repository.delete(post);
        log.info("Publicación eliminada exitosamente: {}", id);
    }

    @Override
    @Transactional
    public MarketplaceResponseDTO updatePostStatus(Long id, PublicationStatus status, Long ownerId) {
        log.info("Actualizando estado de publicación ID: {} a: {}", id, status);

        MarketplacePost post = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Publicación no encontrada: {}", id);
                    return new MarketplaceNotFoundException("Publicación no encontrada con ID: " + id);
                });

        // Verificar que el usuario es el propietario
        if (!post.getOwnerId().equals(ownerId)) {
            log.warn("Usuario {} intentó actualizar publicación del usuario {}", ownerId, post.getOwnerId());
            throw new UnauthorizedException("No tienes permiso para actualizar esta publicación");
        }

        post.setPublicacionEstado(status);
        post.setUpdatedAt(LocalDateTime.now());
        
        MarketplacePost updatedPost = repository.save(post);
        log.info("Estado de publicación actualizado: {}", id);

        return mapToResponseDTO(updatedPost);
    }

    @Override
    public List<MarketplaceResponseDTO> getUserPostsByStatus(Long ownerId, PublicationStatus status) {
        log.debug("Obteniendo publicaciones del usuario {} con estado: {}", ownerId, status);
        return repository.findAllByOwnerIdAndPublicacionEstado(ownerId, status)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MarketplaceResponseDTO> getActiveSkinPosts(String skinId) {
        log.debug("Obteniendo publicaciones activas de la skin: {}", skinId);
        return repository.findAllBySkinIdAndPublicacionEstado(skinId, PublicationStatus.ACTIVE)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Mapea una entidad MarketplacePost a su DTO de respuesta
     */
    private MarketplaceResponseDTO mapToResponseDTO(MarketplacePost post) {
        return MarketplaceResponseDTO.builder()
                .id(post.getId())
                .ownerId(post.getOwnerId())
                .skinId(post.getSkinId())
                .nombreSkin(post.getNombreSkin())
                .descripcion(post.getDescripcion())
                .precio(post.getPrecio())
                .publicacionEstado(post.getPublicacionEstado())
                .publicacionFecha(post.getPublicacionFecha())
                .nombreUsuario(post.getNombreUsuario())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
