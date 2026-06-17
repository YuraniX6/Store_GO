package com.storego.ratingservice.service;

import com.storego.ratingservice.dto.RatingRequest;
import com.storego.ratingservice.dto.RatingResponse;
import com.storego.ratingservice.dto.SkinRatingSummaryResponse;
import com.storego.ratingservice.entity.Rating;
import com.storego.ratingservice.exception.RatingNotFoundException;
import com.storego.ratingservice.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Clase de SERVICIO: aquí vive toda la lógica de negocio del microservicio.
 *
 * El Controller (RatingController) NO debe contener lógica de negocio, solo
 * recibe la petición HTTP y delega el trabajo real a esta clase. Esto separa
 * responsabilidades: el Controller habla "HTTP", el Service habla "reglas
 * de negocio", y el Repository habla "base de datos".
 */
@Service // Marca esta clase como un componente de servicio administrado por Spring.
@RequiredArgsConstructor // Lombok: genera el constructor con los campos "final" (inyecta RatingRepository automáticamente).
@Slf4j // Lombok: crea un logger "log" para registrar mensajes en consola.
@Transactional // Cada método público de esta clase se ejecuta dentro de una transacción de base de datos por defecto.
public class RatingService {

    private final RatingRepository ratingRepository;

    // Método auxiliar (privado) para convertir una entidad Rating (de la base
    // de datos) a un DTO RatingResponse (lo que se le devuelve al cliente).
    // Esto evita exponer directamente la entidad JPA en la respuesta de la API.
    private RatingResponse mapToResponse(Rating rating) {
        return RatingResponse.builder()
                .id(rating.getId())
                .skinId(rating.getSkinId())
                .userId(rating.getUserId())
                .score(rating.getScore())
                .comment(rating.getComment())
                .createdAt(rating.getCreatedAt())
                .updatedAt(rating.getUpdatedAt())
                .build();
    }

    /**
     * Crea una nueva calificación o actualiza la existente si el usuario ya
     * había calificado esta skin (upsert: 1 rating por usuario y skin).
     *
     * Lógica:
     * 1. Se busca si ya existe un Rating de este usuario para esta skin.
     * 2. Si existe -> se actualizan sus campos (score, comment) y se guarda (UPDATE).
     * 3. Si no existe -> se crea un Rating nuevo y se guarda (INSERT).
     */
    public RatingResponse createOrUpdate(UUID userId, RatingRequest request) {
        log.info("Creating/updating rating for skin: {} by user: {}", request.getSkinId(), userId);

        Rating rating = ratingRepository.findBySkinIdAndUserId(request.getSkinId(), userId)
                // .map() se ejecuta SOLO si ya existía una calificación previa (Optional con valor):
                .map(existing -> {
                    log.info("Existing rating found, updating it");
                    existing.setScore(request.getScore());
                    existing.setComment(request.getComment());
                    return existing;
                })
                // .orElseGet() se ejecuta SOLO si no existía ninguna calificación previa (Optional vacío):
                .orElseGet(() -> {
                    log.info("No existing rating found, creating a new one");
                    return Rating.builder()
                            .skinId(request.getSkinId())
                            .userId(userId)
                            .score(request.getScore())
                            .comment(request.getComment())
                            .build();
                });

        // save() funciona tanto para INSERT (entidad nueva, sin id) como para
        // UPDATE (entidad existente, ya con id) — JPA decide automáticamente.
        Rating saved = ratingRepository.save(rating);

        log.info("Rating saved with id: {} for skin: {} by user: {}", saved.getId(), saved.getSkinId(), userId);

        return mapToResponse(saved);
    }

    // Obtiene todas las calificaciones realizadas por el usuario autenticado (GET /ratings/me).
    @Transactional(readOnly = true) // Optimización: como solo se lee, no se necesita transacción de escritura.
    public List<RatingResponse> getMyRatings(UUID userId) {
        log.info("Fetching all ratings for user: {}", userId);

        List<Rating> ratings = ratingRepository.findAllByUserId(userId);

        log.info("Found {} ratings for user: {}", ratings.size(), userId);

        // Convierte cada entidad Rating de la lista a su DTO RatingResponse correspondiente.
        return ratings.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Obtiene el resumen público de calificaciones de una skin: promedio,
     * total de calificaciones y el detalle de cada una (GET /ratings/skin/{skinId}).
     * Este endpoint es público, no requiere JWT (ver SecurityConfig).
     */
    @Transactional(readOnly = true)
    public SkinRatingSummaryResponse getSkinSummary(UUID skinId) {
        log.info("Fetching rating summary for skin: {}", skinId);

        List<Rating> ratings = ratingRepository.findAllBySkinId(skinId);
        Double average = ratingRepository.findAverageScoreBySkinId(skinId);

        log.info("Found {} ratings for skin: {}", ratings.size(), skinId);

        return SkinRatingSummaryResponse.builder()
                .skinId(skinId)
                // Si la skin no tiene ninguna calificación, average viene null desde la BD;
                // en ese caso se devuelve 0.0 en lugar de null, para que sea más fácil de
                // usar en el frontend (ej. mostrar "0 estrellas" en vez de manejar null).
                .averageScore(average != null ? average : 0.0)
                .totalRatings(ratings.size())
                .ratings(ratings.stream().map(this::mapToResponse).toList())
                .build();
    }

    // Elimina una calificación, validando que pertenezca al usuario autenticado
    // (un usuario NUNCA puede borrar la calificación de otro usuario).
    public void delete(UUID userId, UUID ratingId) {
        log.info("Deleting rating: {} for user: {}", ratingId, userId);

        // findByIdAndUserId valida en una sola consulta tanto que el rating exista
        // como que pertenezca al usuario; si no cumple ambas condiciones, lanza la excepción.
        Rating rating = ratingRepository.findByIdAndUserId(ratingId, userId)
                .orElseThrow(() -> {
                    log.warn("Rating not found: {}", ratingId);
                    return new RatingNotFoundException("Rating not found: " + ratingId);
                });

        ratingRepository.delete(rating);

        log.info("Rating deleted: {} for user: {}", ratingId, userId);
    }
}