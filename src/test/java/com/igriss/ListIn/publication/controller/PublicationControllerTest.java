package com.igriss.ListIn.publication.controller;

import com.igriss.ListIn.publication.dto.PublicationRequestDTO;
import com.igriss.ListIn.publication.dto.PublicationResponseDTO;
import com.igriss.ListIn.publication.dto.UpdatePublicationRequestDTO;
import com.igriss.ListIn.publication.dto.page.PageResponse;
import com.igriss.ListIn.publication.entity.PublicationAttributeValue;
import com.igriss.ListIn.publication.repository.PublicationAttributeValueRepository;
import com.igriss.ListIn.publication.service.PublicationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PublicationControllerTest {

    @Mock
    private PublicationService publicationService;

    @Mock
    private PublicationAttributeValueRepository repo;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PublicationController controller;

    private UUID publicationId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        publicationId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void testSavePublication() {
        PublicationRequestDTO request = new PublicationRequestDTO();
        UUID expectedId = UUID.randomUUID();

        when(publicationService.savePublication(request, authentication)).thenReturn(expectedId);

        ResponseEntity<UUID> response = controller.savePublication(request, authentication);

        assertEquals(201, response.getStatusCode().value());
        assertEquals(expectedId, response.getBody());
    }

    @Test
    void testGetPublicationsOfUser() {
        PageResponse<PublicationResponseDTO> pageResponse = new PageResponse<>();
        when(publicationService.findAllByUser(0, 10, authentication)).thenReturn(pageResponse);

        ResponseEntity<PageResponse<PublicationResponseDTO>> response =
                controller.getPublicationsOfUser(0, 10, authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(pageResponse, response.getBody());

        verify(publicationService, times(1)).findAllByUser(0, 10, authentication);
    }

    @Test
    void testGetPublicationById() {
        PublicationResponseDTO responseDTO = PublicationResponseDTO.builder().id(publicationId).build();
        when(publicationService.getById(publicationId, authentication)).thenReturn(responseDTO);

        ResponseEntity<PublicationResponseDTO> response = controller.getPublicationById(publicationId, authentication);

        Assertions.assertNotNull(response.getBody());
        assertEquals(publicationId, response.getBody().getId());
    }

    @Test
    void testGetPAV() {
        PublicationAttributeValue pav = new PublicationAttributeValue();
        when(repo.findByPublication_Id(publicationId)).thenReturn(List.of(pav));

        ResponseEntity<List<PublicationAttributeValue>> response = controller.getPAV(publicationId);

        Assertions.assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testUpdatePublication() {
        UpdatePublicationRequestDTO updateRequest = new UpdatePublicationRequestDTO();
        PublicationResponseDTO responseDTO = PublicationResponseDTO.builder().id(publicationId).build();
        when(publicationService.updateUserPublication(publicationId, updateRequest, authentication)).thenReturn(responseDTO);

        ResponseEntity<PublicationResponseDTO> response = controller.updatePublication(publicationId, updateRequest, authentication);

        Assertions.assertNotNull(response.getBody());
        assertEquals(publicationId, response.getBody().getId());
    }

    @Test
    void testDeletePublication() {
        ResponseEntity<Object> response = controller.deletePublication(publicationId, authentication);

        verify(publicationService, times(1)).deletePublication(publicationId, authentication);
        assertEquals("Publication deleted successfully", response.getBody());
    }

    @Test
    void testViewPublication() {
        when(publicationService.viewPublication(publicationId, authentication)).thenReturn(publicationId);

        ResponseEntity<UUID> response = controller.viewPublication(publicationId, authentication);

        assertEquals(publicationId, response.getBody());
    }

    @Test
    void testLikePublication() {
        when(publicationService.likePublication(publicationId, authentication)).thenReturn(publicationId);

        UUID response = controller.likePublication(publicationId, authentication);

        assertEquals(publicationId, response);
    }

    @Test
    void testUnLikePublication() {
        when(publicationService.unLikePublication(publicationId, authentication)).thenReturn(publicationId);

        UUID response = controller.unLikePublication(publicationId, authentication);

        assertEquals(publicationId, response);
    }

    @Test
    void testGetLikedPublications() {
        PageResponse<PublicationResponseDTO> pageResponse = new PageResponse<>();
        when(publicationService.findAllLikedPublications(0, 5, authentication)).thenReturn(pageResponse);

        PageResponse<PublicationResponseDTO> response = controller.getLikedPublications(0, 5, authentication);

        assertEquals(pageResponse, response);
    }

    @Test
    void testFindByUser() {
        PageResponse<PublicationResponseDTO> pageResponse = new PageResponse<>();
        when(publicationService.findAllByUserId(userId, 0, 10, authentication)).thenReturn(pageResponse);

        PageResponse<PublicationResponseDTO> response = controller.findByUser(userId, 0, 10, authentication);

        assertEquals(pageResponse, response);
    }

    @Test
    void testGetVideos() {
        PageResponse<PublicationResponseDTO> pageResponse = new PageResponse<>();
        when(publicationService.findPublicationsContainingVideo(0, 10, authentication, null)).thenReturn(pageResponse);

        PageResponse<PublicationResponseDTO> response = controller.getVideos(null, 0, 10, authentication);

        assertEquals(pageResponse, response);
    }

    @Test
    void testGetFollowingsPublications() {
        PageResponse<PublicationResponseDTO> pageResponse = new PageResponse<>();
        when(publicationService.getFollowingsPublications(0, 10, authentication)).thenReturn(pageResponse);

        ResponseEntity<PageResponse<PublicationResponseDTO>> response = controller.getFollowingsPublications(0, 10, authentication);

        assertEquals(pageResponse, response.getBody());
    }

    @Test
    void testGetUserPostsContainingVideos() {
        PageResponse<PublicationResponseDTO> pageResponse = new PageResponse<>();
        when(publicationService.getVideoPublications(0, 10, "userIdStr", authentication)).thenReturn(pageResponse);

        ResponseEntity<PageResponse<PublicationResponseDTO>> response =
                controller.getUserPostsContainingVideos(0, 10, "userIdStr", authentication);

        assertEquals(pageResponse, response.getBody());
    }

    @Test
    void testGetUserPostsContainingPhotos() {
        PageResponse<PublicationResponseDTO> pageResponse = new PageResponse<>();
        when(publicationService.getPhotoPublications(0, 10, "userIdStr", authentication)).thenReturn(pageResponse);

        ResponseEntity<PageResponse<PublicationResponseDTO>> response =
                controller.getUserPostsContainingPhotos(0, 10, "userIdStr", authentication);

        assertEquals(pageResponse, response.getBody());
    }
}
