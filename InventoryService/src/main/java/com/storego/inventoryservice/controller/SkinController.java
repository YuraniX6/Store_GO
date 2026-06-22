package com.storego.inventoryservice.controller;

import com.storego.inventoryservice.dto.CreateSkinRequest;
import com.storego.inventoryservice.dto.SkinResponse;
import com.storego.inventoryservice.dto.UpdateSkinRequest;
import com.storego.inventoryservice.service.SkinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/skins")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Skins", description = "API por el manejo de skins en el inventario del usuario")
@SecurityRequirement(name = "Bearer Authentication")
public class SkinController {

    private final SkinService skinService;

    @PostMapping
    @Operation(summary = "Crear un nuevo skin", description = "Crea un nuevo skin en el inventario del usuario. ownerId se extrae del JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Skin creado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SkinResponse.class))),
            @ApiResponse(responseCode = "400", description = "Cuerpo de solicitud inválido o falla en la validación"),
            @ApiResponse(responseCode = "401", description = "No autorizado - token JWT inválido o faltante")
    })
    public ResponseEntity<SkinResponse> createSkin(
            @Valid @RequestBody CreateSkinRequest request,
            Authentication authentication) {
        log.info("POST /skins - Creating new skin");

        UUID ownerId = extractUserIdFromAuth(authentication);
        SkinResponse response = skinService.create(ownerId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    @Operation(summary = "List all my skins", description = "Retrieves all skins belonging to the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de temas del usuario (puede estar vacía)", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = SkinResponse.class)))),
            @ApiResponse(responseCode = "401", description = "No autorizado - token JWT inválido o faltante")
    })
    public ResponseEntity<List<SkinResponse>> getMySkins(Authentication authentication) {
        log.info("GET /skins/me - Retrieving all skins for user");

        UUID ownerId = extractUserIdFromAuth(authentication);
        List<SkinResponse> response = skinService.getMySkins(ownerId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get skin by ID", description = "Retrieves a specific skin by its ID. Only the owner can access their skin.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Skin found and returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SkinResponse.class))),
            @ApiResponse(responseCode = "400", description = "Formato de UUID inválido en el parámetro de ruta"),
            @ApiResponse(responseCode = "401", description = "No autorizado - token JWT inválido o faltante"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - el skin pertenece a otro usuario"),
            @ApiResponse(responseCode = "404", description = "Skin not found")
    })
    public ResponseEntity<SkinResponse> getSkin(
            @PathVariable String id,
            Authentication authentication) {
        log.info("GET /skins/{} - Retrieving skin", id);

        UUID skinId = parseUuid(id);
        UUID ownerId = extractUserIdFromAuth(authentication);
        SkinResponse response = skinService.getSkin(ownerId, skinId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a skin", description = "Updates an existing skin. Only the owner can update their skin.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Skin updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SkinResponse.class))),
            @ApiResponse(responseCode = "400", description = "UUID inválido o falla en la validación del cuerpo de la solicitud"),
            @ApiResponse(responseCode = "401", description = "No autorizado - token JWT inválido o faltante"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - el skin pertenece a otro usuario"),
            @ApiResponse(responseCode = "404", description = "Skin no encontrado")
    })
    public ResponseEntity<SkinResponse> updateSkin(
            @PathVariable String id,
            @Valid @RequestBody UpdateSkinRequest request,
            Authentication authentication) {
        log.info("PUT /skins/{} - Updating skin", id);

        UUID skinId = parseUuid(id);
        UUID ownerId = extractUserIdFromAuth(authentication);
        SkinResponse response = skinService.update(ownerId, skinId, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a skin", description = "Deletes a skin from the user's inventory. Only the owner can delete their skin.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Skin deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Formato de UUID inválido en el parámetro de ruta"),
            @ApiResponse(responseCode = "401", description = "No autorizado - token JWT inválido o faltante"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - el skin pertenece a otro usuario"),
            @ApiResponse(responseCode = "404", description = "Skin no encontrado")
    })
    public ResponseEntity<Void> deleteSkin(
            @PathVariable String id,
            Authentication authentication) {
        log.info("DELETE /skins/{} - Deleting skin", id);

        UUID skinId = parseUuid(id);
        UUID ownerId = extractUserIdFromAuth(authentication);
        skinService.delete(ownerId, skinId);

        return ResponseEntity.noContent().build();
    }

    private UUID extractUserIdFromAuth(Authentication authentication) {
        String principal = (String) authentication.getPrincipal();
        return UUID.fromString(principal);
    }

    private UUID parseUuid(String uuidString) {
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format: {}", uuidString);
            throw new IllegalArgumentException("Invalid UUID format: " + uuidString, e);
        }
    }
}
