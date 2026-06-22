package com.storego.cartService.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;
import com.storego.cartService.dto.CartRequest;
import com.storego.cartService.dto.CartResponse;
import com.storego.cartService.service.CartService;


@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Tag(name = "Cart")
public class CartController {

    private final CartService service;

    @PostMapping
    @Operation(summary = "Agregar producto al carrito")
    public ResponseEntity<CartResponse> add(
            @Valid @RequestBody CartRequest request) {

        return ResponseEntity.ok(
                service.add(request)
        );
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Obtener carrito por usuario")
    public ResponseEntity<List<CartResponse>> getUserCart(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                service.findByUser(userId)
        );
    }
}