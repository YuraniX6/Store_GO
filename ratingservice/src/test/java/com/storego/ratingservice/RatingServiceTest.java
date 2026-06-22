package com.storego.ratingservice;

import com.storego.ratingservice.dto.RatingRequest;
import com.storego.ratingservice.dto.RatingResponse;
import com.storego.ratingservice.dto.SkinRatingSummaryResponse;
import com.storego.ratingservice.entity.Rating;
import com.storego.ratingservice.exception.RatingNotFoundException;
import com.storego.ratingservice.repository.RatingRepository;
import com.storego.ratingservice.service.RatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
// Activamos Mockito para que podamos simular el RatingRepository
// sin necesitar conexión a base de datos en los tests.
@ExtendWith(MockitoExtension.class)
@DisplayName("RatingService - Pruebas unitarias")
class RatingServiceTest {
    // Mock del repositorio: simula las consultas a la BD sin conectarse realmente.
        @Mock
    private RatingRepository ratingRepository;
    // La instancia real del servicio que queremos probar.
    // Mockito le inyectará automáticamente el ratingRepository falso.
    @InjectMocks
    private RatingService ratingService;

    // IDs de prueba generados aleatoriamente para cada ejecución de test.
    private UUID userId;
    private UUID skinId;
    private UUID ratingId;
    private RatingRequest request;
    private Rating existingRating;

    // Se ejecuta antes de cada test para preparar los datos de prueba.
    @BeforeEach
    void setUp() {
        userId   = UUID.randomUUID();
        skinId   = UUID.randomUUID();
        ratingId = UUID.randomUUID();

        // Request que simula lo que envía el usuario al calificar una skin.
        request = RatingRequest.builder()
                .skinId(skinId)
                .score(4)
                .comment("Muy buena skin")
                .build();

        // Rating que simula uno ya existente en la base de datos (score anterior = 3).
        existingRating = Rating.builder()
                .id(ratingId)
                .skinId(skinId)
                .userId(userId)
                .score(3)
                .comment("Comentario anterior")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("createOrUpdate() - Debe CREAR un rating nuevo si el usuario no había calificado antes")
    void testCreateOrUpdate_CreatesNewRating() {
        // DADO: el repositorio no encuentra ningún rating previo para esta skin y usuario.
        // Optional.empty() simula que la consulta a la BD no encontró nada.
        when(ratingRepository.findBySkinIdAndUserId(skinId, userId))
                .thenReturn(Optional.empty());

        // Simulamos el rating nuevo que devolvería la BD al guardarlo.
        Rating newRating = Rating.builder()
                .id(UUID.randomUUID()).skinId(skinId).userId(userId)
                .score(4).comment("Muy buena skin")
                .createdAt(Instant.now()).updatedAt(Instant.now()).build();

        when(ratingRepository.save(any(Rating.class))).thenReturn(newRating);

        // CUANDO: el usuario intenta calificar la skin.
        RatingResponse result = ratingService.createOrUpdate(userId, request);

        // ENTONCES: se crea un rating nuevo con el score y comentario enviados.
        assertNotNull(result);
        assertEquals(4, result.getScore());
        assertEquals("Muy buena skin", result.getComment());
        assertEquals(skinId, result.getSkinId());

        // Verificamos que save() fue llamado una vez (se creó el nuevo rating).
        verify(ratingRepository, times(1)).save(any(Rating.class));
    }

    @Test
    @DisplayName("createOrUpdate() - Debe ACTUALIZAR el rating si el usuario ya había calificado (upsert)")
    void testCreateOrUpdate_UpdatesExistingRating() {
        // DADO: ya existe un rating previo con score=3.
        // Optional.of() simula que la consulta encontró un rating existente.
        when(ratingRepository.findBySkinIdAndUserId(skinId, userId))
                .thenReturn(Optional.of(existingRating));

        // El rating actualizado que devolvería la BD (score cambiado a 4).
        Rating updatedRating = Rating.builder()
                .id(ratingId).skinId(skinId).userId(userId)
                .score(4).comment("Muy buena skin")
                .createdAt(existingRating.getCreatedAt()).updatedAt(Instant.now()).build();

        when(ratingRepository.save(existingRating)).thenReturn(updatedRating);

        // CUANDO: el usuario vuelve a calificar la misma skin.
        RatingResponse result = ratingService.createOrUpdate(userId, request);

        // ENTONCES: el score se actualiza a 4 pero el id es el mismo rating (no se crea uno nuevo).
        assertNotNull(result);
        assertEquals(4, result.getScore());
        assertEquals(ratingId, result.getId()); // Mismo id = se actualizó, no se creó

        // save() se llamó con el rating existente (update, no insert).
        verify(ratingRepository, times(1)).save(existingRating);
    }

    @Test
    @DisplayName("getMyRatings() - Debe retornar todas las calificaciones del usuario")
    void testGetMyRatings_ReturnsUserRatings() {
        // DADO: el usuario tiene 2 ratings en la BD.
        Rating rating2 = Rating.builder()
                .id(UUID.randomUUID()).skinId(UUID.randomUUID()).userId(userId)
                .score(5).comment("Excelente").createdAt(Instant.now()).updatedAt(Instant.now()).build();

        when(ratingRepository.findAllByUserId(userId))
                .thenReturn(List.of(existingRating, rating2));

        // CUANDO: pedimos todos los ratings del usuario.
        List<RatingResponse> results = ratingService.getMyRatings(userId);

        // ENTONCES: retorna una lista con los 2 ratings.
        assertNotNull(results);
        assertEquals(2, results.size());

        verify(ratingRepository, times(1)).findAllByUserId(userId);
    }

    @Test
    @DisplayName("getSkinSummary() - Debe calcular el promedio correctamente")
    void testGetSkinSummary_CalculatesCorrectly() {
        // DADO: hay 2 ratings para esta skin (scores 3 y 5), promedio = 4.0.
        Rating rating2 = Rating.builder()
                .id(UUID.randomUUID()).skinId(skinId).userId(UUID.randomUUID())
                .score(5).comment("Top").createdAt(Instant.now()).updatedAt(Instant.now()).build();

        when(ratingRepository.findAllBySkinId(skinId))
                .thenReturn(List.of(existingRating, rating2));
        // Simulamos el promedio calculado en la BD via query JPQL.
        when(ratingRepository.findAverageScoreBySkinId(skinId))
                .thenReturn(4.0);

        // CUANDO: pedimos el resumen de calificaciones de la skin.
        SkinRatingSummaryResponse summary = ratingService.getSkinSummary(skinId);
        // ENTONCES: el resumen tiene el promedio correcto y el total de ratings.
        assertNotNull(summary);
        assertEquals(skinId, summary.getSkinId());
        assertEquals(4.0, summary.getAverageScore());
        assertEquals(2, summary.getTotalRatings());
    }

    @Test
    @DisplayName("getSkinSummary() - Debe retornar 0.0 si la skin no tiene calificaciones")
    void testGetSkinSummary_NoRatingsReturnsZero() {
        // DADO: la skin no tiene ningún rating aún.
        when(ratingRepository.findAllBySkinId(skinId)).thenReturn(List.of());
        // La query de promedio retorna null cuando no hay datos en la BD.
        when(ratingRepository.findAverageScoreBySkinId(skinId)).thenReturn(null);

        // CUANDO: pedimos el resumen de calificaciones.
        SkinRatingSummaryResponse summary = ratingService.getSkinSummary(skinId);

        // ENTONCES: el promedio debe ser 0.0 (no null) para facilitar su uso en el frontend.
        assertEquals(0.0, summary.getAverageScore());
        assertEquals(0, summary.getTotalRatings());
    }
    @Test
    @DisplayName("delete() - Debe lanzar excepción si el rating no existe o no pertenece al usuario")
    void testDelete_ThrowsIfNotFound() {
        // DADO: no se encuentra ningún rating con ese id para ese usuario.
        when(ratingRepository.findByIdAndUserId(ratingId, userId))
                .thenReturn(Optional.empty());

        // CUANDO/ENTONCES: el servicio debe lanzar RatingNotFoundException.
        // assertThrows verifica que se lanza la excepción esperada.
        RatingNotFoundException ex = assertThrows(
                RatingNotFoundException.class,
                () -> ratingService.delete(userId, ratingId)
        );
        // El mensaje de la excepción debe incluir el id del rating.
        assertTrue(ex.getMessage().contains(ratingId.toString()));
        // Verificamos que delete() nunca fue llamado (no se borró nada).
        verify(ratingRepository, never()).delete(any());
    }
    @Test
    @DisplayName("delete() - Debe eliminar el rating si el usuario es el dueño")
    void testDelete_Success() {
        // DADO: el rating existe y pertenece al usuario autenticado.
        when(ratingRepository.findByIdAndUserId(ratingId, userId))
                .thenReturn(Optional.of(existingRating));
        doNothing().when(ratingRepository).delete(existingRating);
        // CUANDO: el usuario intenta eliminar su propio rating.
        ratingService.delete(userId, ratingId);
        // ENTONCES: el repositorio recibe la orden de eliminar el rating exactamente una vez.
        verify(ratingRepository, times(1)).delete(existingRating);
    }
}