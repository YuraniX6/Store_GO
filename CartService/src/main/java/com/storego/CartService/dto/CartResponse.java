package com.storego.cartService.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CartResponse {
    Long id;
    Long userId;
    Long publicationId;
    Integer quantity;
}
