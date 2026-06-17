package com.storego.ratingservice.controller;

import com.storego.ratingservice.dto.RatingRequest;
import com.storego.ratingservice.dto.RatingResponse;
import com.storego.ratingservice.dto.SkinRatingSummaryResponse;
import com.storego.ratingservice.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST: es la "puerta de entrada" HTTP del microservicio.
 *
 * Define las rutas (endpoints) disponibles bajo el prefijo /ratings, recibe
 * las peticiones del cliente, valida lo básico, y delega todo el trabajo
 * real al RatingService. No contiene lógica de negocio.
 *
 * Las anotaciones @Operation, @ApiResponse, @Tag, etc. son de Swagger/OpenAPI
 * y sirven para generar la documentación interactiva visible en /swagger-ui.html.
 */
@RestController // Combina @Controller + @ResponseBody: cada método devuelve JSON directamente.
@RequestMapping("/ratings") // Todas las rutas de esta clase empiezan con /ratings.
@RequiredArgsConstructor // Lombok: inyecta RatingService por constructor automáticamente.
@Slf4j
@Tag(name = "Ratings", description = "API endpoints for managing skin ratings")
@SecurityRequirement(name = "Bearer Authentication") // Por defecto, todos los endpoints de esta clase requieren JWT (salvo que se indique lo contrario, ver getSkinSummary).
public class RatingController {

    private final RatingService ratingService;

    // POST /ratings -> Crea o actualiza la calificación del usuario autenticado para una skin.
    @PostMapping
    @Operation(summary = "Create or update a rating",
            description = "Creates a new rating for a skin, or updates the existing one if the authenticated user already rated this skin (upsert). userId is extracted from JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rating created or updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RatingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token")
    })
    public ResponseEntity<RatingResponse> createOrUpdateRating(
            @Valid @RequestBody RatingRequest request, // @Valid activa las validaciones definidas en RatingRequest (@NotNull, @Min, @Max, @Size).
            Authentication authentication) { // Spring inyecta automáticamente la autenticación que dejó JwtAuthenticationFilter.
        log.info("POST /ratings - Creating or updating rating");

        UUID userId = extractUserIdFromAuth(authentication);
        RatingResponse response = ratingService.createOrUpdate(userId, request);

        return ResponseEntity.ok(response);
    }

    // GET /ratings/me -> Devuelve todas las calificaciones hechas por el usuario autenticado.
    @GetMapping("/me")
    @Operation(summary = "List my ratings", description = "Retrieves all ratings made by the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of user's ratings (may be empty)", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RatingResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token")
    })
    public ResponseEntity<List<RatingResponse>> getMyRatings(Authentication authentication) {
        log.info("GET /ratings/me - Retrieving all ratings for user");

        UUID userId = extractUserIdFromAuth(authentication);
        List<RatingResponse> response = ratingService.getMyRatings(userId);

        return ResponseEntity.ok(response);
    }

    // GET /ratings/skin/{skinId} -> Endpoint PÚBLICO (sin JWT) que muestra el
    // promedio y el detalle de calificaciones de una skin. Pensado para mostrar
    // las "estrellas" de un producto en el catálogo, visible para cualquier visitante.
    @GetMapping("/skin/{skinId}")
    @SecurityRequirements // Sobrescribe el @SecurityRequirement de la clase: este endpoint específico NO requiere JWT.
    @Operation(summary = "Get rating summary for a skin",
            description = "Public endpoint. Retrieves the average score, total count and detail of all ratings for a given skin.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rating summary returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SkinRatingSummaryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid UUID format in path parameter")
    })
    public ResponseEntity<SkinRatingSummaryResponse> getSkinSummary(@PathVariable String skinId) {
        log.info("GET /ratings/skin/{} - Retrieving rating summary", skinId);

        UUID parsedSkinId = parseUuid(skinId);
        SkinRatingSummaryResponse response = ratingService.getSkinSummary(parsedSkinId);

        return ResponseEntity.ok(response);
    }

    // DELETE /ratings/{id} -> Elimina una calificación. Solo el dueño puede borrarla
    // (la validación de propiedad ocurre dentro de RatingService.delete()).
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a rating", description = "Deletes a rating. Only the owner can delete their rating.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Rating deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid UUID format in path parameter"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Rating not found")
    })
    public ResponseEntity<Void> deleteRating(
            @PathVariable String id,
            Authentication authentication) {
        log.info("DELETE /ratings/{} - Deleting rating", id);

        UUID ratingId = parseUuid(id);
        UUID userId = extractUserIdFromAuth(authentication);
        ratingService.delete(userId, ratingId);

        // 204 No Content: la operación fue exitosa pero no hay nada que devolver en el body.
        return ResponseEntity.noContent().build();
    }

    // Extrae el ID del usuario autenticado desde el objeto Authentication
    // que dejó JwtAuthenticationFilter (el "principal" es el userId como String).
    private UUID extractUserIdFromAuth(Authentication authentication) {
        String principal = (String) authentication.getPrincipal();
        return UUID.fromString(principal);
    }

    // Convierte un String de la URL a UUID, lanzando un error claro si el
    // formato no es válido (en vez de un error genérico de Spring).
    private UUID parseUuid(String uuidString) {
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format: {}", uuidString);
            throw new IllegalArgumentException("Invalid UUID format: " + uuidString, e);
        }
    }
}
