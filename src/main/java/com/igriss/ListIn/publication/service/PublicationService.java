package com.igriss.ListIn.publication.service;

import com.igriss.ListIn.publication.dto.PublicationRequestDTO;
import com.igriss.ListIn.publication.dto.PublicationResponseDTO;
import com.igriss.ListIn.publication.dto.UpdatePublicationRequestDTO;
import com.igriss.ListIn.publication.dto.page.PageResponse;
import com.igriss.ListIn.publication.entity.Publication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface PublicationService {
    UUID savePublication(PublicationRequestDTO request, Authentication connectedUser);

    PageResponse<PublicationResponseDTO> findAllByUser(int page, int size, Authentication connectedUser);

    PageResponse<PublicationResponseDTO> findAllByUserId(UUID userId, Integer page, Integer size, Authentication connectedUser);

    PublicationResponseDTO updateUserPublication(UUID publicationId, UpdatePublicationRequestDTO updatePublication, Authentication authentication);

    PageResponse<PublicationResponseDTO> findPublicationsContainingVideo(int page, int size, Authentication authentication, UUID pCategory);

    UUID likePublication(UUID publicationId, Authentication connectedUser);

    PageResponse<PublicationResponseDTO> findAllLikedPublications(Integer page, Integer size, Authentication authentication);

    ResponseEntity<Object> deletePublication(UUID publicationId, Authentication authentication);

    UUID unLikePublication(UUID publicationId, Authentication connectedUser);

    UUID viewPublication(UUID publicationId, Authentication connectedUser);

    PublicationResponseDTO getById(UUID publicationId, Authentication connectedUser);
    Publication getByIdAsEntity(UUID publicationId);


    PageResponse<PublicationResponseDTO> getFollowingsPublications(int page, int size, Authentication connectedUser);

    PageResponse<PublicationResponseDTO> getVideoPublications(int page, int size,String userId,  Authentication connectedUser);

    PageResponse<PublicationResponseDTO> getPhotoPublications(int page, int size, String userId, Authentication authentication);
}
