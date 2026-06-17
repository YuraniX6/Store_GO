package com.storego.ratingservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA que representa la calificación que un usuario le da a una skin.
 * Cada usuario solo puede tener UNA calificación por skin (constraint único
 * en (skin_id, user_id)). Si el usuario vuelve a calificar, se actualiza
 * la calificación existente (upsert) en lugar de crear una nueva.
 */
@Entity
@Table(name = "ratings", uniqueConstraints = {
        @UniqueConstraint(name = "uk_skin_user", columnNames = {"skin_id", "user_id"})
}, indexes = {
        @Index(name = "idx_skin_id", columnList = "skin_id"),
        @Index(name = "idx_user_id", columnList = "user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    // Identificador único de la calificación, generado automáticamente como UUID.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ID de la skin que está siendo calificada (referencia lógica al InventoryService/CatalogService).
    @Column(name = "skin_id", nullable = false)
    private UUID skinId;

    // ID del usuario que realiza la calificación, extraído del JWT.
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // Puntuación de la calificación, valor entero entre 1 y 5.
    @Column(name = "score", nullable = false)
    private Integer score;

    // Comentario opcional asociado a la calificación.
    @Column(name = "comment", length = 1000)
    private String comment;

    // Timestamp de creación, gestionado automáticamente por Hibernate.
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Timestamp de última actualización, gestionado automáticamente por Hibernate.
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}