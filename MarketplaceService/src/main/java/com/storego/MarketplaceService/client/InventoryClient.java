package com.storego.MarketplaceService.client;

import com.storego.MarketplaceService.exception.InventoryValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class InventoryClient {
    
    private final WebClient webClient;
    
    @Value("${inventory.service.url:http://localhost:8082}")
    private String inventoryServiceUrl;
    
    public InventoryClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    /**
     * Valida si un usuario es propietario de una skin en el inventario
     * @param ownerId ID del propietario
     * @param skinId ID de la skin
     * @param token Token JWT
     * @return true si el usuario posee la skin, false en caso contrario
     * @throws InventoryValidationException si la validación falla
     */
    public boolean validateSkinOwnership(Long ownerId, String skinId, String token) {
        log.info("Validando propiedad de skin: ownerId={}, skinId={}", ownerId, skinId);
        
        try {
            Boolean response = webClient
                    .get()
                    .uri("{baseUrl}/inventory/validate/{ownerId}/{skinId}", 
                            inventoryServiceUrl, ownerId, skinId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(
                                        new InventoryValidationException(
                                                "Error en validación de inventario: " + body
                                        )
                                ))
                    )
                    .bodyToMono(Boolean.class)
                    .block();
            
            if (response != null && response) {
                log.info("Skin validada exitosamente: ownerId={}, skinId={}", ownerId, skinId);
                return true;
            } else {
                log.warn("Usuario no posee la skin: ownerId={}, skinId={}", ownerId, skinId);
                return false;
            }
        } catch (InventoryValidationException e) {
            log.error("Excepción en validación de inventario", e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado en validación de inventario", e);
            throw new InventoryValidationException(
                    "Error al validar propiedad de skin: " + e.getMessage(), e
            );
        }
    }
    
    /**
     * Obtiene información detallada de una skin del inventario
     * @param skinId ID de la skin
     * @param token Token JWT
     * @return Información de la skin
     */
    public String getSkinInfo(String skinId, String token) {
        log.info("Obteniendo información de skin: skinId={}", skinId);
        
        try {
            return webClient
                    .get()
                    .uri("{baseUrl}/inventory/skins/{skinId}", 
                            inventoryServiceUrl, skinId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(
                                        new InventoryValidationException(
                                                "Error obteniendo información de skin: " + body
                                        )
                                ))
                    )
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("Error obteniendo información de skin", e);
            throw new InventoryValidationException(
                    "Error al obtener información de skin: " + e.getMessage(), e
            );
        }
    }
}
