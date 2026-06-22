package com.storego.CartService;

import com.storego.cartService.dto.CartRequest;
import com.storego.cartService.dto.CartResponse;
import com.storego.cartService.entity.CartItem;
import com.storego.cartService.mapper.CartMapper;
import com.storego.cartService.repository.CartRepository;
import com.storego.cartService.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService - Pruebas unitarias")
class CartServiceTest {

    @Mock
    private CartRepository repository;

    @Mock
    private CartMapper mapper;

    @InjectMocks
    private CartService cartService;

    private CartRequest request;
    private CartItem savedItem;
    private CartResponse expectedResponse;

    @BeforeEach
    void setUp() {
        request = CartRequest.builder()
                .userId(1L)
                .publicationId(10L)
                .quantity(2)
                .build();

        savedItem = CartItem.builder()
                .id(100L)
                .userId(1L)
                .publicationId(10L)
                .quantity(2)
                .createdAt(OffsetDateTime.now())
                .build();

        expectedResponse = CartResponse.builder()
                .id(100L)
                .userId(1L)
                .publicationId(10L)
                .quantity(2)
                .build();
    }

    @Test
    @DisplayName("add() - Debe guardar el item y retornar el CartResponse correcto")
    void testAdd_Success() {
        // Given
        when(repository.save(any(CartItem.class))).thenReturn(savedItem);
        when(mapper.toResponse(savedItem)).thenReturn(expectedResponse);

        CartResponse result = cartService.add(request);
   
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(1L, result.getUserId());
        assertEquals(10L, result.getPublicationId());
        assertEquals(2, result.getQuantity());

        verify(repository, times(1)).save(any(CartItem.class));
        verify(mapper, times(1)).toResponse(savedItem);
    }

    @Test
    @DisplayName("add() - Debe construir el CartItem con los datos del request")
    void testAdd_BuildsItemFromRequest() {
        // Given
        when(repository.save(any(CartItem.class))).thenReturn(savedItem);
        when(mapper.toResponse(any())).thenReturn(expectedResponse);
        cartService.add(request);

        verify(repository).save(argThat(item ->
                item.getUserId().equals(1L) &&
                item.getPublicationId().equals(10L) &&
                item.getQuantity().equals(2)
        ));
    }

    @Test
    @DisplayName("findByUser() - Debe retornar lista de items del usuario")
    void testFindByUser_ReturnsList() {
        CartItem item2 = CartItem.builder()
                .id(101L).userId(1L).publicationId(20L).quantity(1)
                .createdAt(OffsetDateTime.now()).build();

        CartResponse response2 = CartResponse.builder()
                .id(101L).userId(1L).publicationId(20L).quantity(1).build();

        when(repository.findByUserId(1L)).thenReturn(List.of(savedItem, item2));
        when(mapper.toResponse(savedItem)).thenReturn(expectedResponse);
        when(mapper.toResponse(item2)).thenReturn(response2);


        List<CartResponse> results = cartService.findByUser(1L);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(100L, results.get(0).getId());
        assertEquals(101L, results.get(1).getId());

        verify(repository, times(1)).findByUserId(1L);
    }
    @Test
    @DisplayName("findByUser() - Debe retornar lista vacía si el usuario no tiene items")
    void testFindByUser_EmptyList() {
        when(repository.findByUserId(99L)).thenReturn(List.of());

        List<CartResponse> results = cartService.findByUser(99L);
        assertNotNull(results);
        assertTrue(results.isEmpty());

        verify(repository, times(1)).findByUserId(99L);
        verify(mapper, never()).toResponse(any());
    }
}