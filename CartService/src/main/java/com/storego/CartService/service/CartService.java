package com.storego.cartService.service;

import com.storego.cartService.dto.CartRequest;
import com.storego.cartService.dto.CartResponse;
import com.storego.cartService.entity.CartItem;
import com.storego.cartService.mapper.CartMapper;
import com.storego.cartService.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository repository;
    private final CartMapper mapper;

    public CartResponse add(CartRequest request) {

        CartItem item = CartItem.builder()
                .userId(request.getUserId())
                .publicationId(request.getPublicationId())
                .quantity(request.getQuantity())
                .build();

        return mapper.toResponse(
                repository.save(item)
        );
    }

    public List<CartResponse> findByUser(Long userId) {

        return repository.findByUserId(userId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
