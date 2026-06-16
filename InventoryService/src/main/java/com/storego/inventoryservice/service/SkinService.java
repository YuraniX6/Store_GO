package com.storego.inventoryservice.service;

import com.storego.inventoryservice.dto.CreateSkinRequest;
import com.storego.inventoryservice.dto.SkinResponse;
import com.storego.inventoryservice.dto.UpdateSkinRequest;
import com.storego.inventoryservice.entity.Skin;
import com.storego.inventoryservice.exception.SkinNotFoundException;
import com.storego.inventoryservice.repository.SkinRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SkinService {

    private final SkinRepository skinRepository;

    // Método auxiliar para mapear una entidad Skin a un DTO SkinResponse, utilizado para estandarizar las respuestas de la API.
    private SkinResponse mapToResponse(Skin skin) {
        return SkinResponse.builder()
                .id(skin.getId())
                .ownerId(skin.getOwnerId())
                .name(skin.getName())
                .weapon(skin.getWeapon())
                .rarity(skin.getRarity())
                .wear(skin.getWear())
                .floatValue(skin.getFloatValue())
                .imageUrl(skin.getImageUrl())
                .createdAt(skin.getCreatedAt())
                .updatedAt(skin.getUpdatedAt())
                .build();
    }

    // Método auxiliar para validar la propiedad de una skin antes de realizar operaciones de actualización o eliminación.
    // Si la skin no existe o no pertenece al usuario, se lanza una excepción personalizada SkinNotFoundException.
    private Skin getOwnedSkin(UUID ownerId, UUID skinId) {
        return skinRepository.findByIdAndOwnerId(skinId, ownerId)
                .orElseThrow(() -> {
                    log.warn("Skin not found: {}", skinId);
                    return new SkinNotFoundException(
                            "Skin not found: " + skinId);
                });
    }

    // Método para crear una nueva skin en el inventario del usuario. El ownerId se extrae del token JWT y se asocia a la nueva skin.
    public SkinResponse create(UUID ownerId, CreateSkinRequest request) {
        log.info("Creating new skin for owner: {}", ownerId);

        Skin skin = Skin.builder()
                .ownerId(ownerId)
                .name(request.getName())
                .weapon(request.getWeapon())
                .rarity(request.getRarity())
                .wear(request.getWear())
                .floatValue(request.getFloatValue())
                .imageUrl(request.getImageUrl())
                .build();

        Skin savedSkin = skinRepository.save(skin);

        log.info(
                "Skin created with id: {} for owner: {}",
                savedSkin.getId(),
                ownerId
        );

        return mapToResponse(savedSkin);
    }

    // Método para obtener todas las skins pertenecientes a un usuario específico, identificado por su ownerId. Retorna una lista de SkinResponse.
    @Transactional(readOnly = true)
    public List<SkinResponse> getMySkins(UUID ownerId) {
        log.info("Fetching all skins for owner: {}", ownerId);

        List<Skin> skins = skinRepository.findAllByOwnerId(ownerId);

        log.info(
                "Found {} skins for owner: {}",
                skins.size(),
                ownerId
        );

        return skins.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Método para obtener una skin específica por su ID, validando que el usuario autenticado sea el propietario de la skin. Retorna un SkinResponse.
    @Transactional(readOnly = true)
    public SkinResponse getSkin(UUID ownerId, UUID skinId) {
        log.info(
                "Fetching skin: {} for owner: {}",
                skinId,
                ownerId
        );

        Skin skin = getOwnedSkin(ownerId, skinId);

        return mapToResponse(skin);
    }

    // Método para actualizar una skin existente, validando la propiedad y aplicando los cambios proporcionados en el UpdateSkinRequest. Retorna el SkinResponse actualizado.
    public SkinResponse update(
            UUID ownerId,
            UUID skinId,
            UpdateSkinRequest request
    ) {
        log.info(
                "Updating skin: {} for owner: {}",
                skinId,
                ownerId
        );

        Skin skin = getOwnedSkin(ownerId, skinId);

        skin.setName(request.getName());
        skin.setWeapon(request.getWeapon());
        skin.setRarity(request.getRarity());
        skin.setWear(request.getWear());
        skin.setFloatValue(request.getFloatValue());
        skin.setImageUrl(request.getImageUrl());

        Skin updatedSkin = skinRepository.save(skin);

        log.info(
                "Skin updated: {} for owner: {}",
                skinId,
                ownerId
        );

        return mapToResponse(updatedSkin);
    }

    // Método para eliminar una skin del inventario del usuario, validando la propiedad antes de realizar la eliminación. Si la skin no existe o no pertenece al usuario, se lanza una excepción.
    public void delete(UUID ownerId, UUID skinId) {
        log.info(
                "Deleting skin: {} for owner: {}",
                skinId,
                ownerId
        );

        Skin skin = getOwnedSkin(ownerId, skinId);

        skinRepository.delete(skin);

        log.info(
                "Skin deleted: {} for owner: {}",
                skinId,
                ownerId
        );
    }
}