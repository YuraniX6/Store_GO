package com.storego.cartService.mapper;

import org.springframework.stereotype.Component;
import com.storego.cartService.dto.CartResponse;
import com.storego.cartService.entity.CartItem;

@Component
public class CartMapper {

    public CartResponse toResponse(CartItem item) {

        return CartResponse.builder()
                .id(item.getId())
                .userId(item.getUserId())
                .publicationId(item.getPublicationId())
                .quantity(item.getQuantity())
                .build();
    }
}