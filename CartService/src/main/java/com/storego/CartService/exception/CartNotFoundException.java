package com.storego.cartService.exception;

public class CartNotFoundException
        extends RuntimeException {

    public CartNotFoundException(Long id) {

        super("Cart item no encontrado: " + id);
    }
}