package com.storego.ratingservice.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Estructura estándar para representar un error en la respuesta de la API.
 *
 * En vez de devolver un error genérico de Spring (que suele ser muy técnico
 * y poco claro), todos los errores de este microservicio se devuelven con
 * este mismo formato, para que el frontend (o quien consuma la API) siempre
 * sepa qué esperar.
 */
@Data // Lombok: genera getters, setters, toString, equals y hashCode.
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // No incluir en el JSON los campos que sean null (ej. fieldErrors si no hay errores de validación).
public class ErrorResponse {

    // Momento exacto en que ocurrió el error.
    private Instant timestamp;

    // Código de estado HTTP (400, 401, 404, 500, etc.).
    private int status;

    // Nombre corto del error, ej. "Not Found", "Bad Request".
    private String error;

    // Mensaje descriptivo y entendible del error.
    private String message;

    // Ruta (URL) donde ocurrió el error, útil para debug.
    private String path;

    // Mapa de errores de validación por campo (solo se usa cuando falla la
    // validación de un DTO, ej. {"score": "Score must be at least 1"}).
    private Map<String, String> fieldErrors;
}
