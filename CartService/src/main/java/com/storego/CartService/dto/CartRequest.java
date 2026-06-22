package com.storego.cartService.dto;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartRequest {
    @NotNull
    private Long userId;

    @NotNull
    private Long publicationId;

    @NotNull
    private Integer quantity;
}
