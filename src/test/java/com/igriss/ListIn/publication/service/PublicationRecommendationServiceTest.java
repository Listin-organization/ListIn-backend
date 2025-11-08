package com.igriss.ListIn.publication.service;

import com.igriss.ListIn.publication.entity.Publication;
import com.igriss.ListIn.publication.entity.static_entity.Category;
import com.igriss.ListIn.publication.enums.PublicationStatus;
import com.igriss.ListIn.publication.repository.PublicationLikeRepository;
import com.igriss.ListIn.publication.repository.PublicationRepository;
import com.igriss.ListIn.publication.service_impl.PublicationRecommendationServiceImpl;
import com.igriss.ListIn.user.entity.User;
import org.instancio.Instancio;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(InstancioExtension.class)
class PublicationRecommendationServiceTest {

    @Mock
    private PublicationRepository publicationRepository;

    @Mock
    private PublicationLikeRepository publicationLikeRepository;

    @InjectMocks
    private PublicationRecommendationServiceImpl recommendationService;

    private User user;
    private Publication publication1;
    private Publication publication2;
    private Publication publication3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = Instancio.create(User.class);

        Category category1 = Category.builder()
                .id(UUID.randomUUID())
                .name("Electronics")
                .build();

        Category category2 = Category.builder()
                .id(UUID.randomUUID())
                .name("Furniture")
                .build();

        publication1 = Publication.builder()
                .id(UUID.randomUUID())
                .title("Phone")
                .category(category1)
                .likes(10L)
                .views(100L)
                .publicationStatus(PublicationStatus.ACTIVE)
                .datePosted(LocalDateTime.now().minusDays(1))
                .build();

        publication2 = Publication.builder()
                .id(UUID.randomUUID())
                .title("Table")
                .category(category1)
                .likes(5L)
                .views(20L)
                .publicationStatus(PublicationStatus.ACTIVE)
                .datePosted(LocalDateTime.now().minusDays(10))
                .build();

        publication3 = Publication.builder()
                .id(UUID.randomUUID())
                .title("Laptop")
                .category(category2)
                .likes(50L)
                .views(10L)
                .publicationStatus(PublicationStatus.ACTIVE)
                .datePosted(LocalDateTime.now().minusDays(2))
                .build();
    }

    @Test
    void testGetRecommendedPublications_CategoryBased() {
        var likedPublication = mock(com.igriss.ListIn.publication.entity.PublicationLike.class);
        when(likedPublication.getPublication()).thenReturn(publication1);
        when(publicationLikeRepository.findAllByUser(user))
                .thenReturn(List.of(likedPublication));

        when(publicationRepository.findByCategoryInAndPublicationStatus(
                anyList(),
                eq(PublicationStatus.ACTIVE),
                any(PageRequest.class)
        )).thenReturn(new PageImpl<>(List.of(publication1, publication2)));

        List<Publication> result = recommendationService.getRecommendedPublications(user, 0, 4);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        verify(publicationRepository, times(1))
                .findByCategoryInAndPublicationStatus(anyList(), eq(PublicationStatus.ACTIVE), any(PageRequest.class));
    }

    @Test
    void testRelevanceSorting_NewerPublicationGetsHigherScore() {
        List<Publication> publications = List.of(publication1, publication2, publication3);

        var like1 = mock(com.igriss.ListIn.publication.entity.PublicationLike.class);
        when(like1.getPublication()).thenReturn(publication1);
        var like2 = mock(com.igriss.ListIn.publication.entity.PublicationLike.class);
        when(like2.getPublication()).thenReturn(publication3);

        when(publicationLikeRepository.findAllByUser(user))
                .thenReturn(List.of(like1, like2));

        when(publicationRepository.findByCategoryInAndPublicationStatus(
                anyList(),
                eq(PublicationStatus.ACTIVE),
                any(PageRequest.class)
        )).thenReturn(new PageImpl<>(publications));

        List<Publication> result = recommendationService.getRecommendedPublications(user, 0, 4);

        assertEquals(3, result.size());
        assertEquals("Phone", result.get(0).getTitle());
    }

    @Test
    void testGetRecommendedPublications_NoLikes() {
        when(publicationLikeRepository.findAllByUser(user)).thenReturn(Collections.emptyList());
        when(publicationRepository.findByCategoryInAndPublicationStatus(
                anyList(),
                eq(PublicationStatus.ACTIVE),
                any(PageRequest.class)
        )).thenReturn(new PageImpl<>(Collections.emptyList()));

        List<Publication> result = recommendationService.getRecommendedPublications(user, 0, 4);
        assertTrue(result.isEmpty());
    }

    private com.igriss.ListIn.publication.entity.PublicationLike mockLike(Publication pub) {
        var like = mock(com.igriss.ListIn.publication.entity.PublicationLike.class);
        when(like.getPublication()).thenReturn(pub);
        return like;
    }
}
