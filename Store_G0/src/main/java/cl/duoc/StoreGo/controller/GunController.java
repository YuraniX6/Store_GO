package cl.duoc.StoreGo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.StoreGo.dto.ApiResponse;
import cl.duoc.StoreGo.dto.gunDTO;
import cl.duoc.StoreGo.service.AuthService;
import cl.duoc.StoreGo.service.GunService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/guns")
public class GunController {

    private final GunService gunService;
    private final AuthService authService;

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<gunDTO>>> getAllGuns(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        ApiResponse<String> validationResponse = authService.validateToken(token);
        

    if(validationResponse == null || validationResponse.getCode() != 200){
        ApiResponse<List<gunDTO>> errorResponse =
                new ApiResponse<List<gunDTO>>(401, "Token inválido", null);
        return ResponseEntity.status(401).body(errorResponse);
    }

    List<gunDTO> skinsarmas = gunService.getAllGunsDTO();
    ApiResponse<List<gunDTO>> response =
            new ApiResponse<List<gunDTO>>(200, "Listado de skins de armas", skinsarmas);
    return ResponseEntity.ok(response);
    }
/*  
    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verifyConnection(
            @RequestHeader("Authorization") String authHeader) {

        // Extraer el token del encabezado de autorización
        String token = authHeader.replace("Bearer ", "");
        ApiResponse<String> validationResponse = authService.validateToken(token);

        // Validar el token
        if (validationResponse == null || validationResponse.getCode() != 200) {
            ApiResponse<String> errorResponse =
                    new ApiResponse<>(401, "Token inválido", null);
            return ResponseEntity.status(401).body(errorResponse);
        }

        // Llamar al otro servicio o controlador
        String result;
        try {
            result = gunService.verifyOtherService(); // Método que debes implementar en GunService
        } catch (Exception e) {
            ApiResponse<String> errorResponse =
                    new ApiResponse<>(500, "Error al conectar con el otro servicio", null);
            return ResponseEntity.status(500).body(errorResponse);
        }

        // Respuesta exitosa
        ApiResponse<String> response =
                new ApiResponse<>(200, "Conexión exitosa: " + result, null);
        return ResponseEntity.ok(response);
    }
        //REVISAR EL GunService, LA ULTIMA PARTE
    */
}
