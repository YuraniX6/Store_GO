package com.storego.ratingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO publico con el resumen de calificaciones de una skin: promedio,
 * cantidad total de calificaciones y el detalle de cada una.
 *
 * Este DTO es la respuesta del endpoint publico
 * GET /ratings/skin/{skinId}, que se usa por ejemplo para mostrar
 * las "estrellas" promedio de un producto en el catalogo SIN necesitar
 * un token JWT (cualquiera puede consultarlo).
 */
@Data // Lombok: genera getters, setters, toString, equals y hashCode automaticamente.
@NoArgsConstructor // Constructor vacio, necesario para Jackson (JSON -> Java).
@AllArgsConstructor // Constructor con todos los campos.
@Builder // Permite construir el objeto con SkinRatingSummaryResponse.builder()....build()
public class SkinRatingSummaryResponse {

    // ID de la skin sobre la que se calcula el resumen.
    private UUID skinId;

    // Promedio de todas las calificaciones (score) de esta skin.
    // Si no hay ninguna calificacion, este valor sera 0.0.
    private double averageScore;

    // Cantidad total de calificaciones recibidas para esta skin.
    private long totalRatings;

    // Lista con el detalle de cada calificacion individual de esta skin.
    private List<RatingResponse> ratings;
}
