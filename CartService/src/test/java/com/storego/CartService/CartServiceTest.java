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

// @ExtendWith(MockitoExtension.class) activa Mockito para esta clase de tests.
// Mockito nos permite simular dependencias (Repository, Mapper) sin necesitar
// base de datos real. Así probamos solo la lógica del Service de forma aislada.
@ExtendWith(MockitoExtension.class)
@DisplayName("CartService - Pruebas unitarias")
class CartServiceTest {

    // @Mock crea un "doble falso" del Repository.
    // Cuando el Service llame a repository.save(), nosotros controlamos qué retorna,
    // sin que nada se guarde realmente en ninguna base de datos.
    @Mock
    private CartRepository repository;

    // Mock del mapper para controlar cómo convierte entidades a DTOs en los tests.
    @Mock
    private CartMapper mapper;

    // @InjectMocks crea la instancia real de CartService e inyecta los mocks anteriores.
    // Es el objeto que estamos probando de verdad.
    @InjectMocks
    private CartService cartService;

    // Variables compartidas entre todos los tests, para no repetir código.
    private CartRequest request;
    private CartItem savedItem;
    private CartResponse expectedResponse;

    // @BeforeEach se ejecuta antes de cada test individual.
    // Aquí preparamos los datos base que usan todos los tests.
    @BeforeEach
    void setUp() {
        // Creamos un request de prueba simulando lo que enviaría el cliente.
        request = CartRequest.builder()
                .userId(1L)
                .publicationId(10L)
                .quantity(2)
                .build();

        // El item que simularemos que devuelve la base de datos al guardar.
        savedItem = CartItem.builder()
                .id(100L)
                .userId(1L)
                .publicationId(10L)
                .quantity(2)
                .createdAt(OffsetDateTime.now())
                .build();

        // El response que esperamos recibir como resultado final.
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
        // DADO (Given): configuramos los mocks para simular el comportamiento esperado.
        // Cuando el service llame a repository.save() con cualquier CartItem,
        // el mock retornará nuestro savedItem en lugar de ir a la BD.
        when(repository.save(any(CartItem.class))).thenReturn(savedItem);
        // Cuando el mapper convierta el savedItem a DTO, retornará expectedResponse.
        when(mapper.toResponse(savedItem)).thenReturn(expectedResponse);

        // CUANDO (When): ejecutamos el método que queremos probar.
        CartResponse result = cartService.add(request);

        // ENTONCES (Then): verificamos que el resultado sea el esperado.
        assertNotNull(result); // El resultado no debe ser null
        assertEquals(100L, result.getId()); // El id debe ser 100
        assertEquals(1L, result.getUserId()); // El userId debe ser 1
        assertEquals(10L, result.getPublicationId()); // La publicación debe ser 10
        assertEquals(2, result.getQuantity()); // La cantidad debe ser 2

        // También verificamos que el service llamó al repository y al mapper exactamente 1 vez.
        verify(repository, times(1)).save(any(CartItem.class));
        verify(mapper, times(1)).toResponse(savedItem);
    }

    @Test
    @DisplayName("add() - Debe construir el CartItem con los datos del request")
    void testAdd_BuildsItemFromRequest() {
        // DADO: configuramos los mocks básicos.
        when(repository.save(any(CartItem.class))).thenReturn(savedItem);
        when(mapper.toResponse(any())).thenReturn(expectedResponse);

        // CUANDO: llamamos al método add.
        cartService.add(request);

        // ENTONCES: verificamos que el CartItem que llegó al repository
        // tiene exactamente los datos que venían en el request original.
        // argThat() nos permite inspeccionar el argumento que recibió el mock.
        verify(repository).save(argThat(item ->
                item.getUserId().equals(1L) &&
                item.getPublicationId().equals(10L) &&
                item.getQuantity().equals(2)
        ));
    }

    @Test
    @DisplayName("findByUser() - Debe retornar lista de items del usuario")
    void testFindByUser_ReturnsList() {
        // DADO: simulamos que el usuario 1 tiene dos items en el carrito.
        CartItem item2 = CartItem.builder()
                .id(101L).userId(1L).publicationId(20L).quantity(1)
                .createdAt(OffsetDateTime.now()).build();

        CartResponse response2 = CartResponse.builder()
                .id(101L).userId(1L).publicationId(20L).quantity(1).build();

        when(repository.findByUserId(1L)).thenReturn(List.of(savedItem, item2));
        when(mapper.toResponse(savedItem)).thenReturn(expectedResponse);
        when(mapper.toResponse(item2)).thenReturn(response2);

        // CUANDO: pedimos el carrito del usuario 1.
        List<CartResponse> results = cartService.findByUser(1L);

        // ENTONCES: la lista debe tener 2 elementos con los ids correctos.
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(100L, results.get(0).getId());
        assertEquals(101L, results.get(1).getId());

        verify(repository, times(1)).findByUserId(1L);
    }

    @Test
    @DisplayName("findByUser() - Debe retornar lista vacía si el usuario no tiene items")
    void testFindByUser_EmptyList() {
        // Dado que: el usuario 99 no tiene ningún item en el carrito.
        when(repository.findByUserId(99L)).thenReturn(List.of());

        // CUANDO: pedimos su carrito.
        List<CartResponse> results = cartService.findByUser(99L);

        // Entonces esto: retorna lista vacía sin lanzar excepción,
        // y el mapper nunca debe ser llamado si no hay items que convertir.
        assertNotNull(results);
        assertTrue(results.isEmpty());

        verify(repository, times(1)).findByUserId(99L);
        verify(mapper, never()).toResponse(any()); // Comprobamos que el mapper no se usó
    }
}