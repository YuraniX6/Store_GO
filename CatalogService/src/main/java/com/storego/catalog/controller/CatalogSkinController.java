package com.storego.catalog.controller;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import com.storego.catalog.dto.BulkCreateRequest;
import com.storego.catalog.dto.BulkCreateResponse;
import com.storego.catalog.dto.CatalogSkinDetailResponse;
import com.storego.catalog.dto.CatalogSkinSummaryResponse;
import com.storego.catalog.dto.PagedResponse;
import com.storego.catalog.service.CatalogSkinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/catalog/skins")
@RequiredArgsConstructor
@Tag(name = "Catalog", description = "CS:GO skin catalog API")
public class CatalogSkinController {

    private final CatalogSkinService service;

    @GetMapping
    @PreAuthorize("permitAll()")
    @Operation(summary = "Listar skins con filtros y paginación")
    @ApiResponse(responseCode = "200", description = "Lista de skins paginada")
    public ResponseEntity<PagedResponse<CatalogSkinSummaryResponse>> list(
            @RequestParam(required = false) String weapon,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) Boolean stattrak,
            @RequestParam(required = false) Boolean souvenir,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(service.list(weapon, category, rarity, stattrak, souvenir, q, pageable));
    }

    @GetMapping("/all")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Obtener todos los skins sin paginación")
    @ApiResponse(responseCode = "200", description = "Lista completa de skins ordenada por nombre")
    public ResponseEntity<List<CatalogSkinSummaryResponse>> listAll() {
        return ResponseEntity.ok(service.listAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Obtener detalles de un skin por ID")
    @ApiResponse(responseCode = "200", description = "Detalles del skin incluyendo rawData")
    @ApiResponse(responseCode = "404", description = "Skin no encontrado")
    public ResponseEntity<CatalogSkinDetailResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear un nuevo skin (solo ADMIN)")
    @ApiResponse(responseCode = "201", description = "Skin creado")
    @ApiResponse(responseCode = "409", description = "Skin con el mismo ID ya existe")
    public ResponseEntity<CatalogSkinDetailResponse> create(@RequestBody JsonNode body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(body));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Bulk creado de skins (solo ADMIN)", description = "Crea múltiples skins en una sola solicitud")
    @ApiResponse(responseCode = "200", description = "Resultado del bulk create con éxito/error por skin")
    public ResponseEntity<BulkCreateResponse> bulkCreate(@RequestBody @Valid BulkCreateRequest request) {
        return ResponseEntity.ok(service.bulkCreate(request.getSkins()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar un skin (solo ADMIN)")
    @ApiResponse(responseCode = "200", description = "Skin actualizado")
    @ApiResponse(responseCode = "404", description = "Skin no encontrado")
    public ResponseEntity<CatalogSkinDetailResponse> update(
            @PathVariable String id,
            @RequestBody JsonNode body) {
        return ResponseEntity.ok(service.update(id, body));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar un skin (solo ADMIN)")
    @ApiResponse(responseCode = "204", description = "Skin eliminado")
    @ApiResponse(responseCode = "404", description = "Skin not found")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
