package cl.storego.auth.controller;

import cl.storego.auth.dto.ApiResponse;
import cl.storego.auth.dto.UserCredentialsDTO;
import cl.storego.auth.dto.UserDTO;
import cl.storego.auth.model.User;
import cl.storego.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    // Endpoint para registrar usuario
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDTO>> register(@Valid @RequestBody UserCredentialsDTO dto) {
        User newUser = userService.registerUser(dto.getUsername(), dto.getEmail(), dto.getPassword());
        UserDTO userDTO = new UserDTO(newUser.getId(), newUser.getEmail(), newUser.getUsername());

        ApiResponse<UserDTO> response =
                new ApiResponse<>(200, "Usuario registrado correctamente", userDTO);

        return ResponseEntity.ok(response);
    }

    // Endpoint para login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody UserCredentialsDTO dto) {
        String token = userService.login(dto.getUsername(), dto.getPassword());

        if (token != null) {
            ApiResponse<String> response =
                    new ApiResponse<>(200, "Login exitoso", token);
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<String> response =
                    new ApiResponse<>(401, "Credenciales inválidas", null);
            return ResponseEntity.status(401).body(response);
        }
    }

    // Endpoint para listar todos los usuarios
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsersDTO();
        ApiResponse<List<UserDTO>> response =
                new ApiResponse<>(200, "Listado de usuarios", users);
        return ResponseEntity.ok(response);
    }

    // Endpoint para validar token
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<String>> validateToken(@RequestParam String token) {
        boolean valid = userService.validateToken(token);

        if (valid) {
            ApiResponse<String> response =
                    new ApiResponse<>(200, "Token válido", "OK");
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<String> response =
                    new ApiResponse<>(401, "Token inválido", null);
            return ResponseEntity.status(401).body(response);
        }
    }
}
