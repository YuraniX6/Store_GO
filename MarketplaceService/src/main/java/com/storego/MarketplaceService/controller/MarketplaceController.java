package com.storego.MarketplaceService.controller;

import com.storego.MarketplaceService.dto.MarketplaceRequestDTO;
import com.storego.MarketplaceService.dto.MarketplaceResponseDTO;
import com.storego.MarketplaceService.entity.PublicationStatus;
import com.storego.MarketplaceService.exception.UnauthorizedException;
import com.storego.MarketplaceService.security.JwtUtil;
import com.storego.MarketplaceService.service.IMarketplaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/marketplace")
@Slf4j
@Tag(name = "Marketplace", description = "API de Marketplace para gestionar publicaciones de skins")
public class MarketplaceController {

    @Autowired
    private IMarketplaceService marketplaceService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Crea una nueva publicación de marketplace
     */
    @PostMapping("/posts")
    @SecurityRequirement(name = "Bearer JWT")
    @Operation(summary = "Crear una nueva publicación", description = "Crea una nueva publicación de venta de skin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Publicación creada exitosamente",
                    content = @Content(schema = @Schema(implementation = MarketplaceResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "409", description = "El usuario no posee la skin")
    })
    public ResponseEntity<MarketplaceResponseDTO> createPost(
            @Valid @RequestBody MarketplaceRequestDTO requestDTO,
            @RequestHeader("Authorization") String authorizationHeader,
            Authentication authentication) {
        
        log.info("POST /marketplace/posts - Creando nueva publicación");

        try {
            Long userId = jwtUtil.getUserIdFromToken(
                    jwtUtil.extractToken(authorizationHeader)
            );

            if (userId == null) {
                throw new UnauthorizedException("No se pudo extraer el ID de usuario del token");
            }

            MarketplaceResponseDTO response = marketplaceService.createPost(
                    userId,
                    requestDTO,
                    jwtUtil.extractToken(authorizationHeader)
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            log.error("Error creando publicación: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Obtiene todas las publicaciones con paginación
     */
    @GetMapping("/posts")
    @Operation(summary = "Listar todas las publicaciones", description = "Obtiene todas las publicaciones del marketplace con paginación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Publicaciones obtenidas exitosamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos")
    })
    public ResponseEntity<Page<MarketplaceResponseDTO>> getAllPosts(
            @Parameter(description = "Número de página (comienza en 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de la página")
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("GET /marketplace/posts - Listando publicaciones, page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<MarketplaceResponseDTO> posts = marketplaceService.getAllPosts(pageable);

        return ResponseEntity.ok(posts);
    }

    /**
     * Obtiene una publicación por ID
     */
    @GetMapping("/posts/{id}")
    @Operation(summary = "Obtener publicación por ID", description = "Obtiene los detalles de una publicación específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Publicación encontrada"),
            @ApiResponse(responseCode = "404", description = "Publicación no encontrada")
    })
    public ResponseEntity<MarketplaceResponseDTO> getPostById(
            @Parameter(description = "ID de la publicación")
            @PathVariable Long id) {
        
        log.info("GET /marketplace/posts/{} - Obteniendo publicación", id);

        return marketplaceService.getPostById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Obtiene las publicaciones de una skin
     */
    @GetMapping("/posts/weapon/{skinId}")
    @Operation(summary = "Obtener publicaciones por skin", description = "Obtiene todas las publicaciones de una skin específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Publicaciones encontradas"),
            @ApiResponse(responseCode = "400", description = "ID de skin inválido")
    })
    public ResponseEntity<Page<MarketplaceResponseDTO>> getPostsBySkin(
            @Parameter(description = "ID de la skin")
            @PathVariable String skinId,
            @Parameter(description = "Número de página")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de la página")
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("GET /marketplace/posts/weapon/{} - Buscando publicaciones de skin", skinId);

        Pageable pageable = PageRequest.of(page, size);
        Page<MarketplaceResponseDTO> posts = marketplaceService.getPostsBySkinId(skinId, pageable);

        return ResponseEntity.ok(posts);
    }

    /**
     * Obtiene las publicaciones activas
     */
    @GetMapping("/posts/active")
    @Operation(summary = "Obtener publicaciones activas", description = "Obtiene todas las publicaciones con estado ACTIVE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Publicaciones activas obtenidas exitosamente")
    })
    public ResponseEntity<Page<MarketplaceResponseDTO>> getActivePosts(
            @Parameter(description = "Número de página")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de la página")
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("GET /marketplace/posts/active - Obteniendo publicaciones activas");

        Pageable pageable = PageRequest.of(page, size);
        Page<MarketplaceResponseDTO> posts = marketplaceService.getActivePosts(pageable);

        return ResponseEntity.ok(posts);
    }

    /**
     * Obtiene las publicaciones del usuario autenticado
     */
    @GetMapping("/my-posts")
    @SecurityRequirement(name = "Bearer JWT")
    @Operation(summary = "Obtener mis publicaciones", description = "Obtiene todas las publicaciones del usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Publicaciones obtenidas exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<Page<MarketplaceResponseDTO>> getMyPosts(
            @RequestHeader("Authorization") String authorizationHeader,
            @Parameter(description = "Número de página")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de la página")
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("GET /marketplace/my-posts - Obteniendo publicaciones del usuario");

        Long userId = jwtUtil.getUserIdFromToken(
                jwtUtil.extractToken(authorizationHeader)
        );

        Pageable pageable = PageRequest.of(page, size);
        Page<MarketplaceResponseDTO> posts = marketplaceService.getPostsByOwnerId(userId, pageable);

        return ResponseEntity.ok(posts);
    }

    /**
     * Obtiene las publicaciones del usuario por estado
     */
    @GetMapping("/my-posts/{status}")
    @SecurityRequirement(name = "Bearer JWT")
    @Operation(summary = "Obtener mis publicaciones por estado", description = "Obtiene las publicaciones del usuario con un estado específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Publicaciones obtenidas exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "400", description = "Estado inválido")
    })
    public ResponseEntity<List<MarketplaceResponseDTO>> getMyPostsByStatus(
            @Parameter(description = "Estado de la publicación: ACTIVE, SOLD, CANCELLED")
            @PathVariable PublicationStatus status,
            @RequestHeader("Authorization") String authorizationHeader) {
        
        log.info("GET /marketplace/my-posts/{} - Obteniendo publicaciones por estado", status);

        Long userId = jwtUtil.getUserIdFromToken(
                jwtUtil.extractToken(authorizationHeader)
        );

        List<MarketplaceResponseDTO> posts = marketplaceService.getUserPostsByStatus(userId, status);

        return ResponseEntity.ok(posts);
    }

    /**
     * Actualiza el estado de una publicación
     */
    @PutMapping("/posts/{id}/status")
    @SecurityRequirement(name = "Bearer JWT")
    @Operation(summary = "Actualizar estado de publicación", description = "Actualiza el estado de una publicación específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "No tienes permiso para actualizar esta publicación"),
            @ApiResponse(responseCode = "404", description = "Publicación no encontrada")
    })
    public ResponseEntity<MarketplaceResponseDTO> updatePostStatus(
            @Parameter(description = "ID de la publicación")
            @PathVariable Long id,
            @Parameter(description = "Nuevo estado: ACTIVE, SOLD, CANCELLED")
            @RequestParam PublicationStatus status,
            @RequestHeader("Authorization") String authorizationHeader) {
        
        log.info("PUT /marketplace/posts/{}/status - Actualizando estado a {}", id, status);

        Long userId = jwtUtil.getUserIdFromToken(
                jwtUtil.extractToken(authorizationHeader)
        );

        MarketplaceResponseDTO updated = marketplaceService.updatePostStatus(id, status, userId);

        return ResponseEntity.ok(updated);
    }

    /**
     * Elimina una publicación
     */
    @DeleteMapping("/posts/{id}")
    @SecurityRequirement(name = "Bearer JWT")
    @Operation(summary = "Eliminar publicación", description = "Elimina una publicación específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Publicación eliminada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "No tienes permiso para eliminar esta publicación"),
            @ApiResponse(responseCode = "404", description = "Publicación no encontrada")
    })
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "ID de la publicación")
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorizationHeader) {
        
        log.info("DELETE /marketplace/posts/{} - Eliminando publicación", id);

        Long userId = jwtUtil.getUserIdFromToken(
                jwtUtil.extractToken(authorizationHeader)
        );

        marketplaceService.deletePost(id, userId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene las publicaciones activas de una skin
     */
    @GetMapping("/skins/{skinId}/active")
    @Operation(summary = "Obtener skins activas", description = "Obtiene todas las publicaciones activas de una skin específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Publicaciones activas obtenidas exitosamente"),
            @ApiResponse(responseCode = "400", description = "ID de skin inválido")
    })
    public ResponseEntity<List<MarketplaceResponseDTO>> getActiveSkinPosts(
            @Parameter(description = "ID de la skin")
            @PathVariable String skinId) {
        
        log.info("GET /marketplace/skins/{}/active - Obteniendo skins activas", skinId);

        List<MarketplaceResponseDTO> posts = marketplaceService.getActiveSkinPosts(skinId);

        return ResponseEntity.ok(posts);
    }
}
