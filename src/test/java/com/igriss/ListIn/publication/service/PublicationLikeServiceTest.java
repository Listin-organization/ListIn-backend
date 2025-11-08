package com.igriss.ListIn.publication.service;

import com.igriss.ListIn.exceptions.BadRequestException;
import com.igriss.ListIn.publication.entity.Publication;
import com.igriss.ListIn.publication.entity.PublicationLike;
import com.igriss.ListIn.publication.repository.PublicationLikeRepository;
import com.igriss.ListIn.publication.service_impl.PublicationLikeServiceImpl;
import com.igriss.ListIn.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PublicationLikeServiceTest {

    @Mock
    private PublicationLikeRepository publicationLikeRepository;

    @InjectMocks
    private PublicationLikeServiceImpl publicationLikeService;

    private User user;
    private Publication publication;
    private UUID userId;
    private UUID publicationId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID();
        publicationId = UUID.randomUUID();

        user = User.builder()
                .userId(userId)
                .build();

        publication = Publication.builder()
                .id(publicationId)
                .title("Sample Publication")
                .bargain(true)
                .latitude(1.0)
                .longitude(1.0)
                .locationName("Tashkent")
                .isGrantedForPreciseLocation(true)
                .description("desc")
                .build();
    }


    @Test
    void like_ShouldSave_WhenNotAlreadyLiked() {
        when(publicationLikeRepository.existsByUserAndPublication(user, publication)).thenReturn(false);

        UUID result = publicationLikeService.like(user, publication);

        assertEquals(publicationId, result);
        verify(publicationLikeRepository, times(1)).save(any(PublicationLike.class));
    }

    @Test
    void like_ShouldThrowException_WhenAlreadyLiked() {
        when(publicationLikeRepository.existsByUserAndPublication(user, publication)).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                publicationLikeService.like(user, publication));

        assertTrue(ex.getMessage().contains("already liked"));
        verify(publicationLikeRepository, never()).save(any());
    }


    @Test
    void unlike_ShouldDelete_WhenPreviouslyLiked() {
        PublicationLike like = PublicationLike.builder()
                .id(UUID.randomUUID())
                .user(user)
                .publication(publication)
                .build();

        when(publicationLikeRepository.existsByUser_UserIdAndPublication_Id(userId, publicationId)).thenReturn(true);
        when(publicationLikeRepository.findByPublication_IdAndUser_UserId(publicationId, userId))
                .thenReturn(Optional.of(like));

        UUID result = publicationLikeService.unlike(publicationId, userId);

        assertEquals(publicationId, result);
        verify(publicationLikeRepository, times(1)).deleteById(like.getId());
    }

    @Test
    void unlike_ShouldThrowException_WhenNotLikedBefore() {
        when(publicationLikeRepository.existsByUser_UserIdAndPublication_Id(userId, publicationId)).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                publicationLikeService.unlike(publicationId, userId));

        assertTrue(ex.getMessage().contains("not liked"));
        verify(publicationLikeRepository, never()).findByPublication_IdAndUser_UserId(any(), any());
    }

    @Test
    void getLikedPublications_ShouldReturnPage() {
        PageRequest pageRequest = PageRequest.of(0, 5);
        Page<PublicationLike> page = new PageImpl<>(List.of(new PublicationLike()));

        when(publicationLikeRepository.findAllByUser(user, pageRequest)).thenReturn(page);

        Page<PublicationLike> result = publicationLikeService.getLikedPublications(user, pageRequest);

        assertEquals(1, result.getTotalElements());
        verify(publicationLikeRepository, times(1)).findAllByUser(user, pageRequest);
    }

    @Test
    void isLiked_ShouldReturnTrue_WhenLiked() {
        when(publicationLikeRepository.existsByPublication_IdAndUser_UserId(publicationId, userId)).thenReturn(true);

        Boolean result = publicationLikeService.isLiked(userId, publicationId);

        assertTrue(result);
        verify(publicationLikeRepository, times(1))
                .existsByPublication_IdAndUser_UserId(publicationId, userId);
    }

    @Test
    void isLiked_ShouldReturnFalse_WhenNotLiked() {
        when(publicationLikeRepository.existsByPublication_IdAndUser_UserId(publicationId, userId)).thenReturn(false);

        Boolean result = publicationLikeService.isLiked(userId, publicationId);

        assertFalse(result);
        verify(publicationLikeRepository, times(1))
                .existsByPublication_IdAndUser_UserId(publicationId, userId);
    }

    @Test
    void deletePublicationLikes_ShouldCallRepository() {
        publicationLikeService.deletePublicationLikes(publicationId);
        verify(publicationLikeRepository, times(1)).deleteByPublicationId(publicationId);
    }
}
