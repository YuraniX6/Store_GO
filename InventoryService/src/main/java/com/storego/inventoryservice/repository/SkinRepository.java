package com.storego.inventoryservice.repository;

import com.storego.inventoryservice.entity.Skin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Skin, proporciona métodos de acceso a datos para realizar operaciones CRUD
 * y consultas específicas relacionadas con las skins de los usuarios.
 */
@Repository
public interface SkinRepository extends JpaRepository<Skin, UUID> {
    // Método personalizado para encontrar todas las skins pertenecientes a un usuario específico, identificado por su ownerId.
    List<Skin> findAllByOwnerId(UUID ownerId);
    /**
     * Método personalizado para encontrar una skin por su ID y el ID del propietario,
     * utilizado para validar la propiedad antes de realizar operaciones de actualización o eliminación.
     */
    Optional<Skin> findByIdAndOwnerId(UUID id, UUID ownerId);
}
