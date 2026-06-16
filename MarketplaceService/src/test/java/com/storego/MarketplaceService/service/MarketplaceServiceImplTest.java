package com.storego.MarketplaceService.service;

import com.storego.MarketplaceService.client.InventoryClient;
import com.storego.MarketplaceService.dto.MarketplaceRequestDTO;
import com.storego.MarketplaceService.dto.MarketplaceResponseDTO;
import com.storego.MarketplaceService.entity.MarketplacePost;
import com.storego.MarketplaceService.entity.PublicationStatus;
import com.storego.MarketplaceService.exception.InventoryValidationException;
import com.storego.MarketplaceService.exception.MarketplaceNotFoundException;
import com.storego.MarketplaceService.exception.UnauthorizedException;
import com.storego.MarketplaceService.repository.MarketplacePostRepository;
import com.storego.MarketplaceService.service.impl.MarketplaceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("MarketplaceService Tests")
class MarketplaceServiceImplTest {

    @Mock
    private MarketplacePostRepository repository;

    @Mock
    private InventoryClient inventoryClient;

    @InjectMocks
    private MarketplaceServiceImpl service;

    private Long userId;
    private String token;
    private MarketplaceRequestDTO requestDTO;
    private MarketplacePost marketplacePost;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = 1L;
        token = "test_token";

        requestDTO = MarketplaceRequestDTO.builder()
                .skinId("skin_001")
                .nombreSkin("Dragon Scale")
                .descripcion("Skin especial")
                .precio(49.99)
                .nombreUsuario("player123")
                .build();

        marketplacePost = MarketplacePost.builder()
                .id(1L)
                .ownerId(userId)
                .skinId("skin_001")
                .nombreSkin("Dragon Scale")
                .descripcion("Skin especial")
                .precio(49.99)
                .publicacionEstado(PublicationStatus.ACTIVE)
                .publicacionFecha(LocalDateTime.now())
                .nombreUsuario("player123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create post successfully when user owns the skin")
    void testCreatePostSuccess() {
        // Arrange
        when(inventoryClient.validateSkinOwnership(userId, "skin_001", token))
                .thenReturn(true);
        when(repository.save(any(MarketplacePost.class)))
                .thenReturn(marketplacePost);

        // Act
        MarketplaceResponseDTO result = service.createPost(userId, requestDTO, token);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Dragon Scale", result.getNombreSkin());
        assertEquals(PublicationStatus.ACTIVE, result.getPublicacionEstado());

        verify(inventoryClient).validateSkinOwnership(userId, "skin_001", token);
        verify(repository).save(any(MarketplacePost.class));
    }

    @Test
    @DisplayName("Should throw InventoryValidationException when user doesn't own the skin")
    void testCreatePostUserDoesntOwnSkin() {
        // Arrange
        when(inventoryClient.validateSkinOwnership(userId, "skin_001", token))
                .thenReturn(false);

        // Act & Assert
        assertThrows(InventoryValidationException.class, 
                () -> service.createPost(userId, requestDTO, token));

        verify(inventoryClient).validateSkinOwnership(userId, "skin_001", token);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete post successfully")
    void testDeletePostSuccess() {
        // Arrange
        when(repository.findById(1L))
                .thenReturn(Optional.of(marketplacePost));

        // Act
        assertDoesNotThrow(() -> service.deletePost(1L, userId));

        // Assert
        verify(repository).delete(marketplacePost);
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when user is not the owner")
    void testDeletePostUnauthorized() {
        // Arrange
        when(repository.findById(1L))
                .thenReturn(Optional.of(marketplacePost));

        // Act & Assert
        assertThrows(UnauthorizedException.class, 
                () -> service.deletePost(1L, 999L));

        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw MarketplaceNotFoundException when post doesn't exist")
    void testDeletePostNotFound() {
        // Arrange
        when(repository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(MarketplaceNotFoundException.class, 
                () -> service.deletePost(999L, userId));

        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Should get all posts with pagination")
    void testGetAllPosts() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        List<MarketplacePost> posts = new ArrayList<>();
        posts.add(marketplacePost);
        Page<MarketplacePost> page = new PageImpl<>(posts, pageable, 1);

        when(repository.findAll(pageable))
                .thenReturn(page);

        // Act
        Page<MarketplaceResponseDTO> result = service.getAllPosts(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Dragon Scale", result.getContent().get(0).getNombreSkin());

        verify(repository).findAll(pageable);
    }

    @Test
    @DisplayName("Should get post by ID")
    void testGetPostById() {
        // Arrange
        when(repository.findById(1L))
                .thenReturn(Optional.of(marketplacePost));

        // Act
        Optional<MarketplaceResponseDTO> result = service.getPostById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Dragon Scale", result.get().getNombreSkin());

        verify(repository).findById(1L);
    }

    @Test
    @DisplayName("Should return empty Optional when post doesn't exist")
    void testGetPostByIdNotFound() {
        // Arrange
        when(repository.findById(999L))
                .thenReturn(Optional.empty());

        // Act
        Optional<MarketplaceResponseDTO> result = service.getPostById(999L);

        // Assert
        assertFalse(result.isPresent());

        verify(repository).findById(999L);
    }

    @Test
    @DisplayName("Should get posts by owner ID with pagination")
    void testGetPostsByOwnerId() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        List<MarketplacePost> posts = new ArrayList<>();
        posts.add(marketplacePost);
        Page<MarketplacePost> page = new PageImpl<>(posts, pageable, 1);

        when(repository.findByOwnerId(userId, pageable))
                .thenReturn(page);

        // Act
        Page<MarketplaceResponseDTO> result = service.getPostsByOwnerId(userId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(repository).findByOwnerId(userId, pageable);
    }

    @Test
    @DisplayName("Should update post status successfully")
    void testUpdatePostStatusSuccess() {
        // Arrange
        when(repository.findById(1L))
                .thenReturn(Optional.of(marketplacePost));
        
        MarketplacePost updatedPost = marketplacePost;
        updatedPost.setPublicacionEstado(PublicationStatus.SOLD);
        
        when(repository.save(any(MarketplacePost.class)))
                .thenReturn(updatedPost);

        // Act
        MarketplaceResponseDTO result = service.updatePostStatus(1L, PublicationStatus.SOLD, userId);

        // Assert
        assertNotNull(result);
        assertEquals(PublicationStatus.SOLD, result.getPublicacionEstado());

        verify(repository).findById(1L);
        verify(repository).save(any(MarketplacePost.class));
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when user is not the owner on status update")
    void testUpdatePostStatusUnauthorized() {
        // Arrange
        when(repository.findById(1L))
                .thenReturn(Optional.of(marketplacePost));

        // Act & Assert
        assertThrows(UnauthorizedException.class, 
                () -> service.updatePostStatus(1L, PublicationStatus.SOLD, 999L));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should get active posts with pagination")
    void testGetActivePosts() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        List<MarketplacePost> posts = new ArrayList<>();
        posts.add(marketplacePost);
        Page<MarketplacePost> page = new PageImpl<>(posts, pageable, 1);

        when(repository.findByPublicacionEstado(PublicationStatus.ACTIVE, pageable))
                .thenReturn(page);

        // Act
        Page<MarketplaceResponseDTO> result = service.getActivePosts(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(PublicationStatus.ACTIVE, result.getContent().get(0).getPublicacionEstado());

        verify(repository).findByPublicacionEstado(PublicationStatus.ACTIVE, pageable);
    }

    @Test
    @DisplayName("Should get user posts by status")
    void testGetUserPostsByStatus() {
        // Arrange
        List<MarketplacePost> posts = new ArrayList<>();
        posts.add(marketplacePost);

        when(repository.findAllByOwnerIdAndPublicacionEstado(userId, PublicationStatus.ACTIVE))
                .thenReturn(posts);

        // Act
        List<MarketplaceResponseDTO> result = service.getUserPostsByStatus(userId, PublicationStatus.ACTIVE);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(PublicationStatus.ACTIVE, result.get(0).getPublicacionEstado());

        verify(repository).findAllByOwnerIdAndPublicacionEstado(userId, PublicationStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should get active skin posts")
    void testGetActiveSkinPosts() {
        // Arrange
        List<MarketplacePost> posts = new ArrayList<>();
        posts.add(marketplacePost);

        when(repository.findAllBySkinIdAndPublicacionEstado("skin_001", PublicationStatus.ACTIVE))
                .thenReturn(posts);

        // Act
        List<MarketplaceResponseDTO> result = service.getActiveSkinPosts("skin_001");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("skin_001", result.get(0).getSkinId());

        verify(repository).findAllBySkinIdAndPublicacionEstado("skin_001", PublicationStatus.ACTIVE);
    }
}
