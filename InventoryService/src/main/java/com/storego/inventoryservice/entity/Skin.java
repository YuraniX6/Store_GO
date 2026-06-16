package com.storego.inventoryservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA que representa una skin en el sistema de inventario.
 * Cada skin está asociada a un usuario propietario y contiene información detallada sobre sus características,
 * como el nombre, arma, rareza, estado de desgaste, valor de desgaste y una URL de imagen representativa.
 * La entidad también incluye timestamps para seguimiento de creación y actualización.
 */
@Entity
@Table(name = "skins", indexes = {
        @Index(name = "idx_owner_id", columnList = "owner_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skin {
    // Identificador único de la skin, generado automáticamente como UUID.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ID del usuario propietario de la skin, se asume que este ID corresponde a un usuario registrado en el sistema de autenticación.
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    // Nombre de la skin, Ejemplo: "Redline", "Asiimov", etc.
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    // Arma asociada a la skin, Ejemplo: AK-47, M4A1-S, etc.
    @Column(name = "weapon", nullable = false, length = 50)
    private String weapon;

    //Rareza de la skin, Ejemplo: Consumer, Industrial, Mil-Spec, Restricted, Classified, Covert, Contraband.
    @Enumerated(EnumType.STRING)
    @Column(name = "rarity", nullable = false)
    private Rarity rarity;

    //Estado de desgaste de la skin, Ejemplo: Factory New, Minimal Wear, Field-Tested, Well-Worn, Battle-Scarred.
    @Enumerated(EnumType.STRING)
    @Column(name = "wear", nullable = false)
    private Wear wear;

    //Valor de desgaste representado como un número decimal entre 0.00 y 1.00, donde 0.00 es Factory New y 1.00 es Battle-Scarred.
    @Column(name = "float_value", nullable = false, precision = 9, scale = 8)
    private BigDecimal floatValue;

    //URL de la imagen representativa de la skin, se asume que esta URL es válida y accesible.
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // Timestamps de creación y última actualización, gestionados automáticamente por Hibernate.
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Timestamp de última actualización, se actualiza automáticamente cada vez que se modifica el registro.
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
