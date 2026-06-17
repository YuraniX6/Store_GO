package com.storego.ratingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO que representa la informacion de una calificacion que se devuelve
 * al cliente como respuesta (por ejemplo, en POST /ratings, GET /ratings/me
 * y dentro del resumen de GET /ratings/skin/{skinId}).
 *
 * Es una "fotografia" de la entidad Rating, pero pensada para ser expuesta
 * por la API (no incluye detalles internos de la base de datos).
 */
@Data // Lombok: genera getters, setters, toString, equals y hashCode automaticamente.
@NoArgsConstructor // Constructor vacio, necesario para que Jackson pueda crear el objeto al convertir JSON -> Java.
@AllArgsConstructor // Constructor con todos los campos.
@Builder // Permite construir el objeto con RatingResponse.builder()....build()
public class RatingResponse {

    // Identificador unico de la calificacion (generado por la base de datos).
    private UUID id;

    // ID de la skin calificada.
    private UUID skinId;

    // ID del usuario que hizo la calificacion.
    private UUID userId;

    // Puntuacion entre 1 y 5.
    private Integer score;

    // Comentario opcional del usuario.
    private String comment;

    // Fecha y hora en que se creo la calificacion.
    private Instant createdAt;

    // Fecha y hora de la ultima actualizacion de la calificacion.
    private Instant updatedAt;
}
