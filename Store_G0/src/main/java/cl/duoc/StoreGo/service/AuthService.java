package cl.duoc.StoreGo.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import cl.duoc.StoreGo.dto.ApiResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final WebClient.Builder webClientBuilder;

    public ApiResponse<String> validateToken(String token) {
        try {
            return webClientBuilder.build().get()
                    .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("login") // nombre pal eureka
                        .path("/validate")
                        .queryParam("token", token)
                        .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<String>>(){})
                    .block();
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al validar el token" + e.getMessage(), null);
        }
    }

}
