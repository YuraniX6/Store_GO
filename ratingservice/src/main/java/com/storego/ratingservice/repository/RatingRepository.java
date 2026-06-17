package com.storego.ratingservice.repository;

import com.storego.ratingservice.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para la entidad Rating, proporciona métodos de acceso a datos
 * para realizar operaciones CRUD y consultas específicas relacionadas con las
 * calificaciones de los usuarios sobre las skins.
 *
 * Al extender JpaRepository<Rating, UUID>, Spring Data JPA ya nos da gratis
 * métodos como save(), findById(), findAll(), delete(), etc. No es necesario
 * implementar nada: Spring genera la implementación automáticamente en tiempo
 * de ejecución, basándose en el nombre de cada método.
 */
@Repository // Marca la interfaz como un componente de acceso a datos administrado por Spring (aunque JpaRepository ya lo implica).
public interface RatingRepository extends JpaRepository<Rating, UUID> {

    // Busca la calificación de un usuario específico para una skin específica.
    // Se usa para implementar el comportamiento de "upsert" (1 rating por usuario y skin):
    // si ya existe, se actualiza; si no existe, se crea una nueva.
    Optional<Rating> findBySkinIdAndUserId(UUID skinId, UUID userId);

    // Obtiene todas las calificaciones realizadas por un usuario (endpoint GET /ratings/me).
    List<Rating> findAllByUserId(UUID userId);

    // Obtiene todas las calificaciones de una skin específica (endpoint público GET /ratings/skin/{skinId}).
    List<Rating> findAllBySkinId(UUID skinId);

    // Busca una calificación por su ID y el ID del usuario, para validar propiedad
    // antes de operaciones de eliminación (un usuario solo puede borrar SU calificación).
    Optional<Rating> findByIdAndUserId(UUID id, UUID userId);

    // Calcula el promedio de las calificaciones (score) de una skin usando JPQL.
    // Devuelve null si la skin todavía no tiene ninguna calificación.
    @org.springframework.data.jpa.repository.Query(
            "SELECT AVG(r.score) FROM Rating r WHERE r.skinId = :skinId")
    Double findAverageScoreBySkinId(UUID skinId);
}
