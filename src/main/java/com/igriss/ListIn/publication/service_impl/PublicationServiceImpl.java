package com.igriss.ListIn.publication.service_impl;

import com.igriss.ListIn.chat.service.ChatRoomService;
import com.igriss.ListIn.exceptions.PublicationNotFoundException;
import com.igriss.ListIn.exceptions.UnauthorizedAccessException;
import com.igriss.ListIn.location.dto.LocationDTO;
import com.igriss.ListIn.location.service.LocationService;
import com.igriss.ListIn.publication.dto.PublicationRequestDTO;
import com.igriss.ListIn.publication.dto.PublicationResponseDTO;
import com.igriss.ListIn.publication.dto.UpdatePublicationRequestDTO;
import com.igriss.ListIn.publication.dto.page.PageResponse;
import com.igriss.ListIn.publication.entity.NumericValue;
import com.igriss.ListIn.publication.entity.Publication;
import com.igriss.ListIn.publication.entity.PublicationAttributeValue;
import com.igriss.ListIn.publication.entity.PublicationImage;
import com.igriss.ListIn.publication.entity.PublicationLike;
import com.igriss.ListIn.publication.entity.PublicationVideo;
import com.igriss.ListIn.publication.mapper.PublicationMapper;
import com.igriss.ListIn.publication.repository.PublicationRepository;
import com.igriss.ListIn.publication.service.NumericValueService;
import com.igriss.ListIn.publication.service.ProductFileService;
import com.igriss.ListIn.publication.service.PublicationAttributeValueService;
import com.igriss.ListIn.publication.service.PublicationLikeService;
import com.igriss.ListIn.publication.service.PublicationService;
import com.igriss.ListIn.publication.service.PublicationViewService;
import com.igriss.ListIn.search.service.PublicationDocumentService;
import com.igriss.ListIn.user.entity.User;
import com.igriss.ListIn.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicationServiceImpl implements PublicationService {

    private final PublicationRepository publicationRepository;

    private final ProductFileService productFileService;
    private final UserService userService;

    private final PublicationDocumentService publicationDocumentService;
    private final PublicationMapper publicationMapper;

    private final LocationService locationService;

    private final NumericValueService numericValueService;

    private final PublicationAttributeValueService publicationAttributeValueService;
    private final PublicationLikeService publicationLikeService;
    private final PublicationViewService publicationViewService;

    private ChatRoomService chatRoomService;

    @Autowired
    public void setChatRoomService(@Lazy ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }


    @Override
    @Transactional
    public UUID savePublication(PublicationRequestDTO request, Authentication authentication) {
        // Extract user from authentication
        User connectedUser = (User) authentication.getPrincipal();

        userService.updateContactDetails(request, connectedUser);

        log.info("Country: {}", request.getCountryName());
        log.info("State: {}", request.getStateName());
        log.info("County: {}", request.getCountyName());

        LocationDTO location = locationService.getLocation(request.getCountryName(), request.getStateName(), request.getCountyName(), "ru");

        // Map and save publication
        Publication publication = publicationMapper.toPublication(request, connectedUser, location);
        publication = publicationRepository.save(publication);

        // Save images //todo -> then removed the assignment

        productFileService.saveImages(request.getImageUrls(), publication);

        // Save video if present
        Publication finalPublication = publication;
        Optional.ofNullable(request.getVideoUrl())
                .filter(url -> !url.isEmpty())
                .ifPresent(url -> productFileService.saveVideo(url, finalPublication));

        List<NumericValue> numericValues = numericValueService.savePublicationNumericValues(request.getNumericValues(), publication);

        // Save attribute values
        List<PublicationAttributeValue> pavList = publicationAttributeValueService.savePublicationAttributeValues(request.getAttributeValues(), publication, numericValues);

        //map into elastic search engine and save publication document
        publicationDocumentService.saveIntoPublicationDocument(publication, pavList, numericValues);

        return publication.getId();
    }

    @Override
    public PageResponse<PublicationResponseDTO> findAllByUser(int page, int size, Authentication connectedUser) {

        User user = (User) connectedUser.getPrincipal();

        Pageable pageable = PageRequest.of(page, size, Sort.by("datePosted").descending());

        Page<Publication> publicationPage = publicationRepository.findAllBySeller(pageable, user);

        List<PublicationResponseDTO> publicationsDTOList = getPublicationResponseDTOS(publicationPage, user);

        return getPageResponse(publicationPage, publicationsDTOList);
    }

    @Override
    public PageResponse<PublicationResponseDTO> findPublicationsContainingVideo(int page, int size, Authentication connectedUser, UUID pCategory) {
        User user = (User) connectedUser.getPrincipal();

        Page<PublicationVideo> publicationVideos;

        if (pCategory != null)
            publicationVideos = productFileService.getVideoPublicationsByParent(pCategory, PageRequest.of(page, size));
        else
            publicationVideos = productFileService.getVideoPublications(PageRequest.of(page, size));

        Page<Publication> publicationPage = publicationVideos.map(
                publicationVideo -> publicationRepository.findById(
                                publicationVideo.getPublication().getId())
                        .orElseThrow(() -> new PublicationNotFoundException(
                                String.format("Publication with id '%s' doesn't exist", publicationVideo.getPublication().getId())))
        );
        List<PublicationResponseDTO> responseDTOList = getPublicationResponseDTOS(publicationPage, user);

        return getPageResponse(publicationPage, responseDTOList);
    }

    @Override
    @Transactional
    public UUID likePublication(UUID publicationId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();

        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new PublicationNotFoundException("No such Publication found"));

        UUID like = publicationLikeService.like(user, publication);

        Integer isUpdated = publicationRepository.incrementLike(publication.getId());

        if (isUpdated != 0)
            log.info("Publication with ID: '{}' UPDATED", publicationId);
        else
            log.warn("Failed to UPDATE publication with ID: '{}'", publicationId);

        return like;
    }

    @Override
    @Transactional
    public UUID unLikePublication(UUID publicationId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();

        if (!publicationRepository.existsById(publicationId)) {
            throw new PublicationNotFoundException("No such Publication found");
        }

        UUID unlike = publicationLikeService.unlike(publicationId, user.getUserId());

        Integer isUpdated = publicationRepository.decrementLike(publicationId);

        if (isUpdated != 0)
            log.info("Publication with ID: '{}' UNLIKED", publicationId);
        else
            log.warn("Failed to UNLIKE publication with ID: '{}'", publicationId);

        return unlike;

    }

    @Override
    @Transactional
    public UUID viewPublication(UUID publicationId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();

        if (!publicationRepository.existsById(publicationId)) {
            log.error("Publication with id: {} does not exist", publicationId);
        }

        return publicationViewService.view(publicationId, user.getUserId());
    }

    @Override
    public Publication getById(UUID publicationId) {
        return publicationRepository.findById(publicationId)
                .orElseThrow(() -> new PublicationNotFoundException("No such Publication found with ID: " + publicationId));
    }

    @Override
    public PageResponse<PublicationResponseDTO> getFollowingsPublications(int page, int size, Authentication connectedUser) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("datePosted").descending());

        User user = (User) connectedUser.getPrincipal();

        List<UUID> followingIds = userService.getFollowings(user.getUserId());

        Page<Publication> publications = publicationRepository.findBySeller_UserIdInOrderByDatePostedDesc(followingIds, pageable);

        List<PublicationResponseDTO> publicationResponseDTOS = getPublicationResponseDTOS(publications, user);

        return getPageResponse(publications, publicationResponseDTOS);
    }

    @Override
    public PageResponse<PublicationResponseDTO> findAllLikedPublications(Integer page, Integer size, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();

        Page<PublicationLike> likedPublications = publicationLikeService.getLikedPublications(user, PageRequest.of(page, size));

        Page<Publication> publicationPage = likedPublications.map(publicationLike ->
                publicationRepository.findById(publicationLike.getPublication().getId()).orElseThrow(() -> new PublicationNotFoundException(
                        String.format("Publication with ID '%s' not found", publicationLike.getPublication().getId()))
                )

        );

        List<PublicationResponseDTO> publicationResponseDTOS = publicationPage.stream()
                .map(publication -> {
                            PublicationResponseDTO publicationResponseDTO = publicationMapper.toPublicationResponseDTO(publication,
                                    productFileService.findImagesByPublicationId(publication.getId()),
                                    productFileService.findVideoUrlByPublicationId(publication.getId()),
                                    numericValueService.findNumericFields(publication.getId()),
                                    true, userService.isFollowingToUser(user.getUserId(), publication.getSeller().getUserId()));

                            publicationResponseDTO.setViews(publicationViewService.views(publication.getId()));

                            publicationResponseDTO.setIsViewed(publicationViewService.isViewed(user.getUserId(), publication.getId()));

                            return publicationResponseDTO;
                        }
                ).toList();

        return getPageResponse(publicationPage, publicationResponseDTOS);
    }


    @Override
    public PageResponse<PublicationResponseDTO> findAllByUserId(UUID userId, Integer page, Integer size, Authentication connectedUser) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("datePosted").descending());

        Page<Publication> publicationPage = publicationRepository.findAllBySeller_UserId(userId, pageable);

        User user = (User) connectedUser.getPrincipal();

        List<PublicationResponseDTO> publicationResponseDTOS = getPublicationResponseDTOS(publicationPage, user);

        return getPageResponse(publicationPage, publicationResponseDTOS);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PublicationResponseDTO updateUserPublication(UUID publicationId, UpdatePublicationRequestDTO updatePublication, Authentication authentication) {

        User connectedUser = (User) authentication.getPrincipal();

        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new PublicationNotFoundException(String.format("Publication with id [%s] does not exist!", publicationId)));

        if (!publication.getSeller().getUserId().equals(connectedUser.getUserId())) {
            log.warn("User with ID: '{}' trying to update publication with id: '{}' which belongs to user with ID: '{}'",
                    connectedUser.getUserId(), publicationId, publication.getSeller().getUserId());
            throw new UnauthorizedAccessException(String.format("Permission denied: you do not have access to update publication with ID %s", publicationId));
        }

        Integer isUpdatedPublication = publicationRepository.updatePublicationById(
                publicationId,
                updatePublication.getTitle(),
                updatePublication.getDescription(),
                updatePublication.getPrice(),
                updatePublication.getBargain(),
                updatePublication.getProductCondition(),
                updatePublication.getAspectRation()
        );

        if (isUpdatedPublication != 0) {
            log.info("Publication updated: {}", publication);
        } else {
            log.info("Publication update failed: {}", publication);
        }

        if (!(updatePublication.getImageUrls() == null || updatePublication.getImageUrls().isEmpty()))
            productFileService.updateImagesByPublication(publication, updatePublication.getImageUrls());


        if (!(updatePublication.getVideoUrl() == null || updatePublication.getVideoUrl().isEmpty()))
            productFileService.updateVideoByPublication(publication, updatePublication.getVideoUrl());

        publicationDocumentService.updateInPublicationDocument(publicationId, updatePublication);


        Publication updatedPublication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new PublicationNotFoundException(String.format("Publication with id [%s] does not exist!", publicationId)));

        List<PublicationImage> images = productFileService.findImagesByPublicationId(updatedPublication.getId());

        String videoUrl = productFileService.findVideoUrlByPublicationId(updatedPublication.getId());

        PublicationResponseDTO publicationResponseDTO = publicationMapper.toPublicationResponseDTO(
                updatedPublication, images, videoUrl, numericValueService.findNumericFields(publication.getId()),
                false, userService.isFollowingToUser(connectedUser.getUserId(), publication.getSeller().getUserId())
        );

        publicationResponseDTO.setViews(publicationViewService.views(publication.getId()));

        publicationResponseDTO.setIsViewed(publicationViewService.isViewed(connectedUser.getUserId(), publication.getId()));

        return publicationResponseDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Object> deletePublication(UUID publicationId, Authentication authentication) {

        return publicationRepository.findById(publicationId).map(publication -> {

            chatRoomService.removeChatRoom(publicationId);
            publicationAttributeValueService.deletePublicationAttributes(publicationId);
            productFileService.deletePublicationFiles(publicationId);
            publicationDocumentService.deleteById(publicationId);
            numericValueService.deletePublicationNumericFields(publicationId);
            publicationLikeService.deletePublicationLikes(publicationId);
            publicationViewService.deletePublicationViews(publicationId);

            publicationRepository.deleteById(publicationId);

            return ResponseEntity.noContent().build();

        }).orElse(ResponseEntity.notFound().build());
    }

    @NotNull
    private PageResponse<PublicationResponseDTO> getPageResponse(Page<Publication> publicationPage, List<PublicationResponseDTO> publicationsDTOList) {
        return new PageResponse<>(
                publicationsDTOList,
                publicationPage.getNumber(),
                publicationPage.getSize(),
                publicationPage.getTotalElements(),
                publicationPage.getTotalPages(),
                publicationPage.isFirst(),
                publicationPage.isLast()
        );
    }

    @NotNull
    public List<PublicationResponseDTO> getPublicationResponseDTOS(Page<Publication> publicationPage, User user) {
        return publicationPage.stream()
                .map(publication -> {

                            PublicationResponseDTO publicationResponseDTO = publicationMapper.toPublicationResponseDTO(publication,

                                    productFileService.findImagesByPublicationId(publication.getId()),
                                    productFileService.findVideoUrlByPublicationId(publication.getId()),
                                    numericValueService.findNumericFields(publication.getId()),
                                    publicationLikeService.isLiked(user.getUserId(), publication.getId()),
                                    userService.isFollowingToUser(user.getUserId(), publication.getSeller().getUserId()));

                            publicationResponseDTO.setViews(publicationViewService.views(publication.getId()));

                            publicationResponseDTO.setIsViewed(publicationViewService.isViewed(user.getUserId(), publication.getId()));

                            return publicationResponseDTO;
                        }
                ).toList();
    }

}
