package com.storego.MarketplaceService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para crear una nueva publicación de marketplace")
public class MarketplaceRequestDTO {
    
    @NotNull(message = "El ID de la skin es requerido")
    @NotBlank(message = "El ID de la skin no puede estar vacío")
    @Schema(description = "ID único de la skin", example = "skin_001")
    private String skinId;

    @NotNull(message = "El nombre de la skin es requerido")
    @NotBlank(message = "El nombre de la skin no puede estar vacío")
    @Size(max = 150, message = "El nombre de la skin no puede exceder 150 caracteres")
    @Schema(description = "Nombre de la skin", example = "Dragon Scale")
    private String nombreSkin;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Schema(description = "Descripción de la skin", example = "Skin exclusiva con efecto especial")
    private String descripcion;

    @NotNull(message = "El precio es requerido")
    @Positive(message = "El precio debe ser mayor a 0")
    @Schema(description = "Precio de venta", example = "49.99")
    private Double precio;

    @NotNull(message = "El nombre de usuario es requerido")
    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    @Schema(description = "Nombre de usuario del vendedor", example = "player123")
    private String nombreUsuario;
}
