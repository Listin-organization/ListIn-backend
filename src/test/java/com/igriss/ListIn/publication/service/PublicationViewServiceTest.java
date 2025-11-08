package com.igriss.ListIn.publication.service;

import com.igriss.ListIn.publication.repository.PublicationViewRepository;
import com.igriss.ListIn.publication.service_impl.PublicationViewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicationViewServiceTest {

    @Mock
    private PublicationViewRepository publicationViewRepository;

    @InjectMocks
    private PublicationViewServiceImpl publicationViewService;

    private UUID publicationId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        publicationId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void testView_ShouldCallUpsertView_AndReturnPublicationId() {
        UUID result = publicationViewService.view(publicationId, userId);

        assertEquals(publicationId, result);
        verify(publicationViewRepository, times(1))
                .upsertView(any(UUID.class), eq(publicationId), eq(userId));
        verifyNoMoreInteractions(publicationViewRepository);
    }

    @Test
    void testViews_ShouldReturnCountFromRepository() {
        when(publicationViewRepository.countAllByPublication_Id(publicationId)).thenReturn(10L);

        Long result = publicationViewService.views(publicationId);

        assertEquals(10L, result);
        verify(publicationViewRepository, times(1))
                .countAllByPublication_Id(publicationId);
        verifyNoMoreInteractions(publicationViewRepository);
    }

    @Test
    void testIsViewed_ShouldReturnTrue_WhenRepositoryReturnsTrue() {
        when(publicationViewRepository.existsByUser_UserIdAndPublication_Id(userId, publicationId))
                .thenReturn(true);

        Boolean result = publicationViewService.isViewed(userId, publicationId);

        assertTrue(result);
        verify(publicationViewRepository, times(1))
                .existsByUser_UserIdAndPublication_Id(userId, publicationId);
        verifyNoMoreInteractions(publicationViewRepository);
    }

    @Test
    void testIsViewed_ShouldReturnFalse_WhenRepositoryReturnsFalse() {
        when(publicationViewRepository.existsByUser_UserIdAndPublication_Id(userId, publicationId))
                .thenReturn(false);

        Boolean result = publicationViewService.isViewed(userId, publicationId);

        assertFalse(result);
        verify(publicationViewRepository, times(1))
                .existsByUser_UserIdAndPublication_Id(userId, publicationId);
        verifyNoMoreInteractions(publicationViewRepository);
    }

    @Test
    void testDeletePublicationViews_ShouldCallRepositoryDelete() {
        publicationViewService.deletePublicationViews(publicationId);

        verify(publicationViewRepository, times(1))
                .deleteAllByPublication_Id(publicationId);
        verifyNoMoreInteractions(publicationViewRepository);
    }
}
