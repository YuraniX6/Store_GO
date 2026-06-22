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

@ExtendWith(MockitoExtension.class)
@DisplayName("RatingService - Pruebas unitarias")
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @InjectMocks
    private RatingService ratingService;

    private UUID userId;
    private UUID skinId;
    private UUID ratingId;
    private RatingRequest request;
    private Rating existingRating;

    @BeforeEach
    void setUp() {
        userId   = UUID.randomUUID();
        skinId   = UUID.randomUUID();
        ratingId = UUID.randomUUID();

        request = RatingRequest.builder()
                .skinId(skinId)
                .score(4)
                .comment("Muy buena skin")
                .build();

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
    @DisplayName("createOrUpdate() - Debe CREAR un rating nuevo si no existía antes")
    void testCreateOrUpdate_CreatesNewRating() {
        when(ratingRepository.findBySkinIdAndUserId(skinId, userId))
                .thenReturn(Optional.empty());

        Rating newRating = Rating.builder()
                .id(UUID.randomUUID()).skinId(skinId).userId(userId)
                .score(4).comment("Muy buena skin")
                .createdAt(Instant.now()).updatedAt(Instant.now()).build();

        when(ratingRepository.save(any(Rating.class))).thenReturn(newRating);


        RatingResponse result = ratingService.createOrUpdate(userId, request);

        assertNotNull(result);
        assertEquals(4, result.getScore());
        assertEquals("Muy buena skin", result.getComment());
        assertEquals(skinId, result.getSkinId());

        verify(ratingRepository, times(1)).save(any(Rating.class));
    }

    @Test
    @DisplayName("createOrUpdate() - Debe ACTUALIZAR el rating si el usuario ya había calificado")
    void testCreateOrUpdate_UpdatesExistingRating() {
        when(ratingRepository.findBySkinIdAndUserId(skinId, userId))
                .thenReturn(Optional.of(existingRating));

        Rating updatedRating = Rating.builder()
                .id(ratingId).skinId(skinId).userId(userId)
                .score(4).comment("Muy buena skin")
                .createdAt(existingRating.getCreatedAt()).updatedAt(Instant.now()).build();

        when(ratingRepository.save(existingRating)).thenReturn(updatedRating);

        RatingResponse result = ratingService.createOrUpdate(userId, request);

        assertNotNull(result);
        assertEquals(4, result.getScore());
        assertEquals(ratingId, result.getId());

        verify(ratingRepository, times(1)).save(existingRating);
    }

    @Test
    @DisplayName("getMyRatings() - Debe retornar todas las calificaciones del usuario")
    void testGetMyRatings_ReturnsUserRatings() {
        Rating rating2 = Rating.builder()
                .id(UUID.randomUUID()).skinId(UUID.randomUUID()).userId(userId)
                .score(5).comment("Excelente").createdAt(Instant.now()).updatedAt(Instant.now()).build();

        when(ratingRepository.findAllByUserId(userId))
                .thenReturn(List.of(existingRating, rating2));

        List<RatingResponse> results = ratingService.getMyRatings(userId);
        assertNotNull(results);
        assertEquals(2, results.size());

        verify(ratingRepository, times(1)).findAllByUserId(userId);
    }

    @Test
    @DisplayName("getSkinSummary() - Debe calcular promedio y total correctamente")
    void testGetSkinSummary_CalculatesCorrectly() {
        Rating rating2 = Rating.builder()
                .id(UUID.randomUUID()).skinId(skinId).userId(UUID.randomUUID())
                .score(5).comment("Top").createdAt(Instant.now()).updatedAt(Instant.now()).build();

        when(ratingRepository.findAllBySkinId(skinId))
                .thenReturn(List.of(existingRating, rating2));
        when(ratingRepository.findAverageScoreBySkinId(skinId))
                .thenReturn(4.0);

     
        SkinRatingSummaryResponse summary = ratingService.getSkinSummary(skinId);

        assertNotNull(summary);
        assertEquals(skinId, summary.getSkinId());
        assertEquals(4.0, summary.getAverageScore());
        assertEquals(2, summary.getTotalRatings());
    }

    @Test
    @DisplayName("getSkinSummary() - Debe retornar 0.0 si la skin no tiene calificaciones")
    void testGetSkinSummary_NoRatingsReturnsZero() {
    
        when(ratingRepository.findAllBySkinId(skinId)).thenReturn(List.of());
        when(ratingRepository.findAverageScoreBySkinId(skinId)).thenReturn(null);

        SkinRatingSummaryResponse summary = ratingService.getSkinSummary(skinId);
        assertEquals(0.0, summary.getAverageScore());
        assertEquals(0, summary.getTotalRatings());
    }

    @Test
    @DisplayName("delete() - Debe lanzar excepción si el rating no pertenece al usuario")
    void testDelete_ThrowsIfNotFound() {
        when(ratingRepository.findByIdAndUserId(ratingId, userId))
                .thenReturn(Optional.empty());
        RatingNotFoundException ex = assertThrows(
                RatingNotFoundException.class,
                () -> ratingService.delete(userId, ratingId)
        );

        assertTrue(ex.getMessage().contains(ratingId.toString()));
        verify(ratingRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete() - Debe eliminar el rating si pertenece al usuario")
    void testDelete_Success() {
        // Given
        when(ratingRepository.findByIdAndUserId(ratingId, userId))
                .thenReturn(Optional.of(existingRating));
        doNothing().when(ratingRepository).delete(existingRating);

        // When
        ratingService.delete(userId, ratingId);

        // Then
        verify(ratingRepository, times(1)).delete(existingRating);
    }
}