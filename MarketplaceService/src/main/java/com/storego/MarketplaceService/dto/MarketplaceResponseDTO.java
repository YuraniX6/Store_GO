package com.storego.MarketplaceService.dto;

import com.storego.MarketplaceService.entity.PublicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para respuestas de publicaciones del marketplace")
public class MarketplaceResponseDTO {
    
    @Schema(description = "ID único de la publicación", example = "1")
    private Long id;

    @Schema(description = "ID del propietario/vendedor", example = "123")
    private Long ownerId;

    @Schema(description = "ID único de la skin", example = "skin_001")
    private String skinId;

    @Schema(description = "Nombre de la skin", example = "Dragon Scale")
    private String nombreSkin;

    @Schema(description = "Descripción de la skin", example = "Skin exclusiva con efecto especial")
    private String descripcion;

    @Schema(description = "Precio de venta", example = "49.99")
    private Double precio;

    @Schema(description = "Estado de la publicación", example = "ACTIVE")
    private PublicationStatus publicacionEstado;

    @Schema(description = "Fecha de publicación", example = "2024-06-15T10:30:00")
    private LocalDateTime publicacionFecha;

    @Schema(description = "Nombre de usuario del vendedor", example = "player123")
    private String nombreUsuario;

    @Schema(description = "Fecha de creación", example = "2024-06-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Fecha de última actualización", example = "2024-06-15T10:30:00")
    private LocalDateTime updatedAt;
}
