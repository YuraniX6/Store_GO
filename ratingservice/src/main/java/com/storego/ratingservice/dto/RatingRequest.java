package com.storego.ratingservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO (Data Transfer Object) que representa el cuerpo (body) que el cliente
 * envia al crear o actualizar una calificacion.
 *
 * Se usa en el endpoint POST /ratings. Las anotaciones de validacion
 * (@NotNull, @Min, @Max, @Size) hacen que Spring valide automaticamente
 * estos campos antes de que lleguen al Controller; si algo no cumple,
 * Spring devuelve un error 400 con el mensaje indicado.
 */
@Data // Lombok: genera automaticamente getters, setters, toString, equals y hashCode.
@NoArgsConstructor // Lombok: genera un constructor vacio (sin parametros), requerido por Jackson para deserializar JSON.
@AllArgsConstructor // Lombok: genera un constructor con todos los campos como parametros.
@Builder // Lombok: permite construir objetos con la sintaxis RatingRequest.builder()....build()
public class RatingRequest {

    // ID de la skin que se va a calificar. Es obligatorio (no puede ser null).
    @NotNull(message = "SkinId cannot be null")
    private UUID skinId;

    // Puntuacion de la calificacion. Debe ser un numero entero entre 1 y 5 (estrellas).
    @NotNull(message = "Score cannot be null")
    @Min(value = 1, message = "Score must be at least 1")
    @Max(value = 5, message = "Score must be at most 5")
    private Integer score;

    // Comentario opcional que el usuario puede dejar junto con su calificacion.
    // Maximo 1000 caracteres.
    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    private String comment;
}
