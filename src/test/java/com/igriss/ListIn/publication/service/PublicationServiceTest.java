package com.igriss.ListIn.publication.service;

import com.igriss.ListIn.exceptions.PublicationNotFoundException;
import com.igriss.ListIn.exceptions.UnauthorizedAccessException;
import com.igriss.ListIn.location.dto.LocationDTO;
import com.igriss.ListIn.location.entity.Country;
import com.igriss.ListIn.location.entity.County;
import com.igriss.ListIn.location.entity.State;
import com.igriss.ListIn.location.service.LocationService;
import com.igriss.ListIn.publication.dto.ProductVariantRequestDTO;
import com.igriss.ListIn.publication.dto.ProductVariantResponseDTO;
import com.igriss.ListIn.publication.dto.PublicationRequestDTO;
import com.igriss.ListIn.publication.dto.PublicationResponseDTO;
import com.igriss.ListIn.publication.dto.UpdatePublicationRequestDTO;
import com.igriss.ListIn.publication.dto.page.PageResponse;
import com.igriss.ListIn.publication.entity.ProductVariant;
import com.igriss.ListIn.publication.entity.Publication;
import com.igriss.ListIn.publication.entity.PublicationLike;
import com.igriss.ListIn.publication.entity.PublicationVideo;
import com.igriss.ListIn.publication.mapper.PublicationMapper;
import com.igriss.ListIn.publication.repository.PublicationRepository;
import com.igriss.ListIn.publication.service_impl.ProductVariantService;
import com.igriss.ListIn.publication.service_impl.PublicationServiceImpl;
import com.igriss.ListIn.search.service.PublicationDocumentService;
import com.igriss.ListIn.user.entity.User;
import com.igriss.ListIn.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicationServiceTest {

    @Mock
    private PublicationRepository publicationRepository;

    @Mock
    private ProductFileService productFileService;

    @Mock
    private UserService userService;

    @Mock
    private PublicationDocumentService publicationDocumentService;

    @Mock
    private PublicationMapper publicationMapper;

    @Mock
    private LocationService locationService;

    @Mock
    private NumericValueService numericValueService;

    @Mock
    private PublicationAttributeValueService publicationAttributeValueService;

    @Mock
    private PublicationLikeService publicationLikeService;

    @Mock
    private PublicationViewService publicationViewService;

    @Mock
    private ProductVariantService productVariantService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PublicationServiceImpl publicationService;

    private User testUser;
    private Publication testPublication;
    private PublicationRequestDTO publicationRequestDTO;
    private UUID testPublicationId;
    private LocationDTO locationDTO;

    @BeforeEach
    void setUp() {
        testPublicationId = UUID.randomUUID();
        testUser = User.builder()
                .userId(UUID.randomUUID())
                .email("testuser")
                .build();

        testPublication = Publication.builder()
                .id(testPublicationId)
                .title("Test Publication")
                .description("Test Description")
                .price(100.0f)
                .bargain(true)
                .likes(0L)
                .seller(testUser)
                .build();

        Country country = Country.builder().id(UUID.randomUUID()).value("Test Country").build();
        State state = State.builder().id(UUID.randomUUID()).value("Test State").build();
        County county = County.builder().id(UUID.randomUUID()).value("Test County").build();

        locationDTO = LocationDTO.builder()
                .country(country)
                .state(state)
                .county(county)
                .build();

        publicationRequestDTO = PublicationRequestDTO.builder()
                .title("Test Publication")
                .description("Test Description")
                .price(100.0f)
                .bargain(true)
                .imageUrls(List.of("url1", "url2"))
                .videoUrl("videoUrl")
                .categoryId(UUID.randomUUID())
                .countryName("Test Country")
                .stateName("Test State")
                .countyName("Test County")
                .numericValues(Collections.emptyList())
                .attributeValues(Collections.emptyList())
                .productVariants(Collections.emptyList())
                .build();
    }

    @Test
    void savePublication_WithValidData_ShouldReturnPublicationId() {
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(locationService.getLocation(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(locationDTO);
        when(publicationMapper.toPublication(any(), any(), any())).thenReturn(testPublication);
        when(publicationRepository.save(any(Publication.class))).thenReturn(testPublication);
        when(numericValueService.savePublicationNumericValues(anyList(), any()))
                .thenReturn(Collections.emptyList());
        when(publicationAttributeValueService.savePublicationAttributeValues(anyList(), any(), anyList()))
                .thenReturn(Collections.emptyList());

        UUID result = publicationService.savePublication(publicationRequestDTO, authentication);

        assertThat(result).isEqualTo(testPublicationId);
        verify(userService).updateContactDetails(publicationRequestDTO, testUser);
        verify(productFileService).saveImages(publicationRequestDTO.getImageUrls(), testPublication);
        verify(productFileService).saveVideo(publicationRequestDTO.getVideoUrl(), testPublication);
        verify(publicationDocumentService).saveIntoPublicationDocument(any(), anyList(), anyList());
    }

    @Test
    void savePublication_WithProductVariants_ShouldSaveVariants() {
        ProductVariantRequestDTO variantDTO = ProductVariantRequestDTO.builder()
                .size("Variant 1")
                .build();
        publicationRequestDTO.setProductVariants(List.of(variantDTO));

        when(authentication.getPrincipal()).thenReturn(testUser);
        when(locationService.getLocation(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(locationDTO);
        when(publicationMapper.toPublication(any(), any(), any())).thenReturn(testPublication);
        when(publicationRepository.save(any(Publication.class))).thenReturn(testPublication);
        when(numericValueService.savePublicationNumericValues(anyList(), any()))
                .thenReturn(Collections.emptyList());
        when(publicationAttributeValueService.savePublicationAttributeValues(anyList(), any(), anyList()))
                .thenReturn(Collections.emptyList());

        publicationService.savePublication(publicationRequestDTO, authentication);

        verify(productVariantService).save(eq(variantDTO), eq(testPublication));
    }

    @Test
    void savePublication_WithEmptyVideoUrl_ShouldNotSaveVideo() {
        publicationRequestDTO.setVideoUrl("");

        when(authentication.getPrincipal()).thenReturn(testUser);
        when(locationService.getLocation(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(locationDTO);
        when(publicationMapper.toPublication(any(), any(), any())).thenReturn(testPublication);
        when(publicationRepository.save(any(Publication.class))).thenReturn(testPublication);
        when(numericValueService.savePublicationNumericValues(anyList(), any()))
                .thenReturn(Collections.emptyList());
        when(publicationAttributeValueService.savePublicationAttributeValues(anyList(), any(), anyList()))
                .thenReturn(Collections.emptyList());

        publicationService.savePublication(publicationRequestDTO, authentication);

        verify(productFileService, never()).saveVideo(anyString(), any());
    }

    @Test
    void findAllByUser_ShouldReturnPageResponse() {
        when(authentication.getPrincipal()).thenReturn(testUser);
        Page<Publication> page = new PageImpl<>(List.of(testPublication));
        when(publicationRepository.findAllBySeller(any(Pageable.class), eq(testUser))).thenReturn(page);
        when(productFileService.findImagesByPublicationId(any())).thenReturn(Collections.emptyList());
        when(productFileService.findVideoUrlByPublicationId(any())).thenReturn(null);
        when(numericValueService.findNumericFields(any())).thenReturn(Collections.emptyList());
        when(publicationLikeService.isLiked(any(), any())).thenReturn(false);
        when(userService.isFollowingToUser(any(), any())).thenReturn(false);
        when(publicationViewService.views(any())).thenReturn(0L);
        when(publicationViewService.isViewed(any(), any())).thenReturn(false);
        when(productVariantService.findByPublicationId(any())).thenReturn(Collections.emptyList());

        PublicationResponseDTO responseDTO = PublicationResponseDTO.builder().id(testPublicationId).build();
        when(publicationMapper.toPublicationResponseDTO(any(), anyList(), any(), anyList(), anyBoolean(), anyBoolean(), anyList()))
                .thenReturn(responseDTO);

        PageResponse<PublicationResponseDTO> result = publicationService.findAllByUser(0, 10, authentication);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getNumber()).isZero();
    }

    @Test
    void findPublicationsContainingVideo_WithCategory_ShouldReturnFilteredPublications() {
        UUID categoryId = UUID.randomUUID();
        when(authentication.getPrincipal()).thenReturn(testUser);

        PublicationVideo video = new PublicationVideo();
        video.setPublication(testPublication);
        Page<PublicationVideo> videoPage = new PageImpl<>(List.of(video));

        when(productFileService.getVideoPublicationsByParent(eq(categoryId), any(PageRequest.class)))
                .thenReturn(videoPage);
        when(publicationRepository.findById(testPublicationId)).thenReturn(Optional.of(testPublication));
        when(productFileService.findImagesByPublicationId(any())).thenReturn(Collections.emptyList());
        when(productFileService.findVideoUrlByPublicationId(any())).thenReturn("video.mp4");
        when(numericValueService.findNumericFields(any())).thenReturn(Collections.emptyList());
        when(publicationLikeService.isLiked(any(), any())).thenReturn(false);
        when(userService.isFollowingToUser(any(), any())).thenReturn(false);
        when(publicationViewService.views(any())).thenReturn(10L);
        when(publicationViewService.isViewed(any(), any())).thenReturn(true);
        when(productVariantService.findByPublicationId(any())).thenReturn(Collections.emptyList());

        PublicationResponseDTO responseDTO = PublicationResponseDTO.builder().id(testPublicationId).build();
        when(publicationMapper.toPublicationResponseDTO(any(), anyList(), any(), anyList(), anyBoolean(), anyBoolean(), anyList()))
                .thenReturn(responseDTO);


        PageResponse<PublicationResponseDTO> result = publicationService.findPublicationsContainingVideo(0, 10, authentication, categoryId);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(productFileService).getVideoPublicationsByParent(eq(categoryId), any(PageRequest.class));
    }

    @Test
    void findPublicationsContainingVideo_WithoutCategory_ShouldReturnAllVideoPublications() {

        when(authentication.getPrincipal()).thenReturn(testUser);

        PublicationVideo video = new PublicationVideo();
        video.setPublication(testPublication);
        Page<PublicationVideo> videoPage = new PageImpl<>(List.of(video));

        when(productFileService.getVideoPublications(any(PageRequest.class))).thenReturn(videoPage);
        when(publicationRepository.findById(testPublicationId)).thenReturn(Optional.of(testPublication));
        when(productFileService.findImagesByPublicationId(any())).thenReturn(Collections.emptyList());
        when(productFileService.findVideoUrlByPublicationId(any())).thenReturn("video.mp4");
        when(numericValueService.findNumericFields(any())).thenReturn(Collections.emptyList());
        when(publicationLikeService.isLiked(any(), any())).thenReturn(false);
        when(userService.isFollowingToUser(any(), any())).thenReturn(false);
        when(publicationViewService.views(any())).thenReturn(5L);
        when(publicationViewService.isViewed(any(), any())).thenReturn(false);
        when(productVariantService.findByPublicationId(any())).thenReturn(Collections.emptyList());

        PublicationResponseDTO responseDTO = PublicationResponseDTO.builder().id(testPublicationId).build();
        when(publicationMapper.toPublicationResponseDTO(any(), anyList(), any(), anyList(), anyBoolean(), anyBoolean(), anyList()))
                .thenReturn(responseDTO);


        PageResponse<PublicationResponseDTO> result = publicationService.findPublicationsContainingVideo(0, 10, authentication, null);


        assertThat(result).isNotNull();
        verify(productFileService).getVideoPublications(any(PageRequest.class));
    }

    @Test
    void findPublicationsContainingVideo_PublicationNotFound_ShouldThrowException() {

        when(authentication.getPrincipal()).thenReturn(testUser);

        PublicationVideo video = new PublicationVideo();
        video.setPublication(testPublication);
        Page<PublicationVideo> videoPage = new PageImpl<>(List.of(video));

        when(productFileService.getVideoPublications(any(PageRequest.class))).thenReturn(videoPage);
        when(publicationRepository.findById(testPublicationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> publicationService.findPublicationsContainingVideo(0, 10, authentication, null))
                .isInstanceOf(PublicationNotFoundException.class)
                .hasMessageContaining("doesn't exist");
    }

    @Test
    void likePublication_WithValidId_ShouldReturnLikeId() {

        UUID likeId = UUID.randomUUID();
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(publicationRepository.findById(testPublicationId)).thenReturn(Optional.of(testPublication));
        when(publicationLikeService.like(testUser, testPublication)).thenReturn(likeId);
        when(publicationRepository.incrementLike(testPublicationId)).thenReturn(1);


        UUID result = publicationService.likePublication(testPublicationId, authentication);


        assertThat(result).isEqualTo(likeId);
        verify(publicationRepository).incrementLike(testPublicationId);
    }

    @Test
    void likePublication_PublicationNotFound_ShouldThrowException() {

        when(authentication.getPrincipal()).thenReturn(testUser);
        when(publicationRepository.findById(testPublicationId)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> publicationService.likePublication(testPublicationId, authentication))
                .isInstanceOf(PublicationNotFoundException.class)
                .hasMessage("No such Publication found");
    }

    @Test
    void likePublication_IncrementFails_ShouldLogWarning() {

        UUID likeId = UUID.randomUUID();
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(publicationRepository.findById(testPublicationId)).thenReturn(Optional.of(testPublication));
        when(publicationLikeService.like(testUser, testPublication)).thenReturn(likeId);
        when(publicationRepository.incrementLike(testPublicationId)).thenReturn(0);


        UUID result = publicationService.likePublication(testPublicationId, authentication);


        assertThat(result).isEqualTo(likeId);
        verify(publicationRepository).incrementLike(testPublicationId);
    }

    @Test
    void unLikePublication_WithValidId_ShouldReturnUnlikeId() {

        UUID unlikeId = UUID.randomUUID();
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(publicationRepository.existsById(testPublicationId)).thenReturn(true);
        when(publicationLikeService.unlike(testPublicationId, testUser.getUserId())).thenReturn(unlikeId);
        when(publicationRepository.decrementLike(testPublicationId)).thenReturn(1);


        UUID result = publicationService.unLikePublication(testPublicationId, authentication);


        assertThat(result).isEqualTo(unlikeId);
        verify(publicationRepository).decrementLike(testPublicationId);
    }

    @Test
    void unLikePublication_PublicationNotFound_ShouldThrowException() {

        when(authentication.getPrincipal()).thenReturn(testUser);
        when(publicationRepository.existsById(testPublicationId)).thenReturn(false);


        assertThatThrownBy(() -> publicationService.unLikePublication(testPublicationId, authentication))
                .isInstanceOf(PublicationNotFoundException.class)
                .hasMessage("No such Publication found");
    }

    @Test
    void unLikePublication_DecrementFails_ShouldLogWarning() {

        UUID unlikeId = UUID.randomUUID();
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(publicationRepository.existsById(testPublicationId)).thenReturn(true);
        when(publicationLikeService.unlike(testPublicationId, testUser.getUserId())).thenReturn(unlikeId);
        when(publicationRepository.decrementLike(testPublicationId)).thenReturn(0);


        UUID result = publicationService.unLikePublication(testPublicationId, authentication);


        assertThat(result).isEqualTo(unlikeId);
        verify(publicationRepository).decrementLike(testPublicationId);
    }

    @Test
    void viewPublication_WithValidId_ShouldReturnViewId() {

        UUID viewId = UUID.randomUUID();
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(publicationRepository.existsById(testPublicationId)).thenReturn(true);
        when(publicationViewService.view(testPublicationId, testUser.getUserId())).thenReturn(viewId);


        UUID result = publicationService.viewPublication(testPublicationId, authentication);


        assertThat(result).isEqualTo(viewId);
        verify(publicationViewService).view(testPublicationId, testUser.getUserId());
    }

    @Test
    void viewPublication_PublicationNotFound_ShouldStillCallViewService() {

        UUID viewId = UUID.randomUUID();
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(publicationRepository.existsById(testPublicationId)).thenReturn(false);
        when(publicationViewService.view(testPublicationId, testUser.getUserId())).thenReturn(viewId);


        UUID result = publicationService.viewPublication(testPublicationId, authentication);


        assertThat(result).isEqualTo(viewId);
    }

    @Test
    void getById_WithValidId_ShouldReturnPublicationResponseDTO() {

        when(authentication.getPrincipal()).thenReturn(testUser);
        when(publicationRepository.findById(testPublicationId)).thenReturn(Optional.of(testPublication));
        when(productFileService.findImagesByPublicationId(testPublicationId)).thenReturn(Collections.emptyList());
        when(productFileService.findVideoUrlByPublicationId(testPublicationId)).thenReturn("video.mp4");
        when(numericValueService.findNumericFields(testPublicationId)).thenReturn(Collections.emptyList());
        when(publicationLikeService.isLiked(testUser.getUserId(), testPublicationId)).thenReturn(true);
        when(userService.isFollowingToUser(testUser.getUserId(), testPublicationId)).thenReturn(false);
        when(productVariantService.findByPublicationId(testPublicationId)).thenReturn(Collections.emptyList());

        PublicationResponseDTO expectedDTO = PublicationResponseDTO.builder()
                .id(testPublicationId)
                .title("Test Publication")
                .build();
        when(publicationMapper.toPublicationResponseDTO(any(), anyList(), anyString(), anyList(), anyBoolean(), anyBoolean(), anyList()))
                .thenReturn(expectedDTO);


        PublicationResponseDTO result = publicationService.getById(testPublicationId, authentication);


        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testPublicationId);
    }

    @Test
    void getById_PublicationNotFound_ShouldThrowException() {

        when(authentication.getPrincipal()).thenReturn(testUser);
        when(publicationRepository.findById(testPublicationId)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> publicationService.getById(testPublicationId, authentication))
                .isInstanceOf(PublicationNotFoundException.class)
                .hasMessageContaining("No such Publication found with ID:");
    }

    @Test
    void getByIdAsEntity_WithValidId_ShouldReturnPublication() {

        when(publicationRepository.findById(testPublicationId)).thenReturn(Optional.of(testPublication));


        Publication result = publicationService.getByIdAsEntity(testPublicationId);


        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testPublicationId);
    }

    @Test
    void getByIdAsEntity_PublicationNotFound_ShouldThrowException() {

        when(publicationRepository.findById(testPublicationId)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> publicationService.getByIdAsEntity(testPublicationId))
                .isInstanceOf(PublicationNotFoundException.class)
                .hasMessageContaining("No such Publication found with ID:");
    }

    @Test
    void getFollowingsPublications_ShouldReturnPageResponse() {

        List<UUID> followingIds = List.of(UUID.randomUUID());
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(userService.getFollowings(testUser.getUserId())).thenReturn(followingIds);

        Page<Publication> page = new PageImpl<>(List.of(testPublication));
        when(publicationRepository.findBySeller_UserIdInOrderByDatePostedDesc(eq(followingIds), any(Pageable.class)))
                .thenReturn(page);

        when(productFileService.findImagesByPublicationId(any())).thenReturn(Collections.emptyList());
        when(productFileService.findVideoUrlByPublicationId(any())).thenReturn(null);
        when(numericValueService.findNumericFields(any())).thenReturn(Collections.emptyList());
        when(publicationLikeService.isLiked(any(), any())).thenReturn(false);
        when(userService.isFollowingToUser(any(), any())).thenReturn(true);
        when(publicationViewService.views(any())).thenReturn(0L);
        when(publicationViewService.isViewed(any(), any())).thenReturn(false);
        when(productVariantService.findByPublicationId(any())).thenReturn(Collections.emptyList());

        PublicationResponseDTO responseDTO = PublicationResponseDTO.builder().id(testPublicationId).build();
        when(publicationMapper.toPublicationResponseDTO(any(), anyList(), any(), anyList(), anyBoolean(), anyBoolean(), anyList()))
                .thenReturn(responseDTO);


        PageResponse<PublicationResponseDTO> result = publicationService.getFollowingsPublications(0, 10, authentication);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getVideoPublications_WithUserId_ShouldReturnPageResponse() {

        String userId = UUID.randomUUID().toString();
        when(authentication.getPrincipal()).thenReturn(testUser);

        Page<Publication> page = new PageImpl<>(List.of(testPublication));
        when(publicationRepository.findPublicationsContainingVideos(eq(UUID.fromString(userId)), any(PageRequest.class)))
                .thenReturn(page);

        mockPublicationResponseMapping();


        PageResponse<PublicationResponseDTO> result = publicationService.getVideoPublications(0, 10, userId, authentication);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getVideoPublications_WithoutUserId_ShouldUseCurrentUser() {

        when(authentication.getPrincipal()).thenReturn(testUser);

        Page<Publication> page = new PageImpl<>(List.of(testPublication));
        when(publicationRepository.findPublicationsContainingVideos(eq(testUser.getUserId()), any(PageRequest.class)))
                .thenReturn(page);

        mockPublicationResponseMapping();


        PageResponse<PublicationResponseDTO> result = publicationService.getVideoPublications(0, 10, null, authentication);


        assertThat(result).isNotNull();
        verify(publicationRepository).findPublicationsContainingVideos(eq(testUser.getUserId()), any(PageRequest.class));
    }

    @Test
    void getPhotoPublications_WithUserId_ShouldReturnPageResponse() {

        String userId = UUID.randomUUID().toString();
        when(authentication.getPrincipal()).thenReturn(testUser);

        Page<Publication> page = new PageImpl<>(List.of(testPublication));
        when(publicationRepository.findPublicationsWithoutVideos(eq(UUID.fromString(userId)), any(PageRequest.class)))
                .thenReturn(page);

        mockPublicationResponseMapping();


        PageResponse<PublicationResponseDTO> result = publicationService.getPhotoPublications(0, 10, userId, authentication);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getPhotoPublications_WithoutUserId_ShouldUseCurrentUser() {

        when(authentication.getPrincipal()).thenReturn(testUser);

        Page<Publication> page = new PageImpl<>(List.of(testPublication));
        when(publicationRepository.findPublicationsWithoutVideos(eq(testUser.getUserId()), any(PageRequest.class)))
                .thenReturn(page);

        mockPublicationResponseMapping();


        PageResponse<PublicationResponseDTO> result = publicationService.getPhotoPublications(0, 10, null, authentication);


        assertThat(result).isNotNull();
        verify(publicationRepository).findPublicationsWithoutVideos(eq(testUser.getUserId()), any(PageRequest.class));
    }

    @Test
    void findAllLikedPublications_ShouldReturnPageResponse() {

        when(authentication.getPrincipal()).thenReturn(testUser);

        PublicationLike like = new PublicationLike();
        like.setPublication(testPublication);
        Page<PublicationLike> likePage = new PageImpl<>(List.of(like));

        when(publicationLikeService.getLikedPublications(eq(testUser), any(PageRequest.class)))
                .thenReturn(likePage);
        when(publicationRepository.findById(testPublicationId)).thenReturn(Optional.of(testPublication));

        mockPublicationResponseMapping();


        PageResponse<PublicationResponseDTO> result = publicationService.findAllLikedPublications(0, 10, authentication);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findAllLikedPublications_PublicationNotFound_ShouldThrowException() {

        when(authentication.getPrincipal()).thenReturn(testUser);

        PublicationLike like = new PublicationLike();
        like.setPublication(testPublication);
        Page<PublicationLike> likePage = new PageImpl<>(List.of(like));

        when(publicationLikeService.getLikedPublications(eq(testUser), any(PageRequest.class)))
                .thenReturn(likePage);
        when(publicationRepository.findById(testPublicationId)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> publicationService.findAllLikedPublications(0, 10, authentication))
                .isInstanceOf(PublicationNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void findAllByUserId_ShouldReturnPageResponse() {

        UUID userId = UUID.randomUUID();
        when(authentication.getPrincipal()).thenReturn(testUser);

        Page<Publication> page = new PageImpl<>(List.of(testPublication));
        when(publicationRepository.findAllBySeller_UserId(eq(userId), any(Pageable.class)))
                .thenReturn(page);

        mockPublicationResponseMapping();


        PageResponse<PublicationResponseDTO> result = publicationService.findAllByUserId(userId, 0, 10, authentication);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void updateUserPublication_WithValidData_ShouldReturnUpdatedPublication() {

        UpdatePublicationRequestDTO updateDTO = UpdatePublicationRequestDTO.builder()
                .title("Updated Title")
                .description("Updated Description")
                .price(200.0f)
                .bargain(false)
                .imageUrls(Map.of(true, List.of("newUrl")))
                .videoUrl(Map.of(true, "newVideo"))
                .productVariants(Collections.emptyList())
                .build();

        when(authentication.getPrincipal()).thenReturn(testUser);
        when(publicationRepository.findById(testPublicationId)).thenReturn(Optional.of(testPublication));
        when(publicationRepository.updatePublicationById(any(), anyString(), anyString(), any(), any(), any(), any(), any()))
                .thenReturn(1);
        when(publicationRepository.findById(testPublicationId)).thenReturn(Optional.of(testPublication));

        mockPublicationResponseMapping();

        PublicationResponseDTO responseDTO = PublicationResponseDTO.builder()
                .id(testPublicationId)
                .title("Updated Title")
                .build();
        when(publicationMapper.toPublicationResponseDTO(any(), anyList(), any(), anyList(), anyBoolean(), anyBoolean(), anyList()))
                .thenReturn(responseDTO);


        PublicationResponseDTO result = publicationService.updateUserPublication(testPublicationId, updateDTO, authentication);


        assertThat(result).isNotNull();
        verify(productFileService).updateImagesByPublication(testPublication, updateDTO.getImageUrls());
        verify(productFileService).updateVideoByPublication(testPublication, updateDTO.getVideoUrl());
        verify(publicationDocumentService).updateInPublicationDocument(testPublicationId, updateDTO);
    }

    @Test
    void updateUserPublication_UnauthorizedUser_ShouldThrowException() {

        User differentUser = User.builder().userId(UUID.randomUUID()).build();
        UpdatePublicationRequestDTO updateDTO = UpdatePublicationRequestDTO.builder()
                .title("Updated Title")
                .build();

        when(authentication.getPrincipal()).thenReturn(differentUser);
        when(publicationRepository.findById(testPublicationId)).thenReturn(Optional.of(testPublication));


        assertThatThrownBy(() -> publicationService.updateUserPublication(testPublicationId, updateDTO, authentication))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("Permission denied");
    }

    @Test
    void updateUserPublication_PublicationNotFound_ShouldThrowException() {

        UpdatePublicationRequestDTO updateDTO = UpdatePublicationRequestDTO.builder()
                .title("Updated Title")
                .build();

        when(authentication.getPrincipal()).thenReturn(testUser);
        when(publicationRepository.findById(testPublicationId)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> publicationService.updateUserPublication(testPublicationId, updateDTO, authentication))
                .isInstanceOf(PublicationNotFoundException.class)
                .hasMessageContaining("does not exist");
    }

    @Test
    void updateUserPublication_WithNullImages_ShouldNotUpdateImages() {

        UpdatePublicationRequestDTO updateDTO = UpdatePublicationRequestDTO.builder()
                .title("Updated Title")
                .description("Updated Description")
                .price(200.0f)
                .imageUrls(null)
                .videoUrl(null)
                .productVariants(Collections.emptyList())
                .build();

        when(authentication.getPrincipal()).thenReturn(testUser);
        when(publicationRepository.findById(testPublicationId)).thenReturn(Optional.of(testPublication));
        when(publicationRepository.updatePublicationById(any(), anyString(), anyString(), any(), any(), any(), any(), any()))
                .thenReturn(1);

        mockPublicationResponseMapping();

        PublicationResponseDTO responseDTO = PublicationResponseDTO.builder().id(testPublicationId).build();
        when(publicationMapper.toPublicationResponseDTO(any(), anyList(), any(), anyList(), anyBoolean(), anyBoolean(), anyList()))
                .thenReturn(responseDTO);


        publicationService.updateUserPublication(testPublicationId, updateDTO, authentication);


        verify(productFileService, never()).updateImagesByPublication(any(), anyMap());
        verify(productFileService, never()).updateVideoByPublication(any(), anyMap());
    }

    @Test
    void updateUserPublication_WithEmptyImages_ShouldNotUpdateImages() {

        UpdatePublicationRequestDTO updateDTO = UpdatePublicationRequestDTO.builder()
                .title("Updated Title")
                .description("Updated Description")
                .price(200.0f)
                .imageUrls(Map.of())
                .videoUrl(Map.of(false, ""))
                .productVariants(Collections.emptyList())
                .build();

        when(authentication.getPrincipal()).thenReturn(testUser);
        when(publicationRepository.findById(testPublicationId)).thenReturn(Optional.of(testPublication));
        when(publicationRepository.updatePublicationById(any(), anyString(), anyString(), any(), any(), any(), any(), any()))
                .thenReturn(1);

        mockPublicationResponseMapping();

        PublicationResponseDTO responseDTO = PublicationResponseDTO.builder().id(testPublicationId).build();
        when(publicationMapper.toPublicationResponseDTO(any(), anyList(), any(), anyList(), anyBoolean(), anyBoolean(), anyList()))
                .thenReturn(responseDTO);


        publicationService.updateUserPublication(testPublicationId, updateDTO, authentication);


        verify(productFileService, never()).updateImagesByPublication(any(), anyMap());
    }

    @Test
    void updateUserPublication_UpdateFails_ShouldStillProceed() {

        UpdatePublicationRequestDTO updateDTO = UpdatePublicationRequestDTO.builder()
                .title("Updated Title")
                .description("Updated Description")
                .price(200.0f)
                .productVariants(Collections.emptyList())
                .build();

        when(authentication.getPrincipal()).thenReturn(testUser);
        when(publicationRepository.findById(testPublicationId)).thenReturn(Optional.of(testPublication));
        when(publicationRepository.updatePublicationById(any(), anyString(), anyString(), any(), any(), any(), any(), any()))
                .thenReturn(0);

        mockPublicationResponseMapping();

        PublicationResponseDTO responseDTO = PublicationResponseDTO.builder().id(testPublicationId).build();
        when(publicationMapper.toPublicationResponseDTO(any(), anyList(), any(), anyList(), anyBoolean(), anyBoolean(), anyList()))
                .thenReturn(responseDTO);


        PublicationResponseDTO result = publicationService.updateUserPublication(testPublicationId, updateDTO, authentication);


        assertThat(result).isNotNull();
    }

    @Test
    void updateUserPublication_SecondFindThrowsException_ShouldThrowException() {

        UpdatePublicationRequestDTO updateDTO = UpdatePublicationRequestDTO.builder()
                .title("Updated Title")
                .description("Updated Description")
                .productVariants(Collections.emptyList())
                .build();

        when(authentication.getPrincipal()).thenReturn(testUser);
        when(publicationRepository.findById(testPublicationId))
                .thenReturn(Optional.of(testPublication))
                .thenReturn(Optional.empty());
        when(publicationRepository.updatePublicationById(any(), anyString(), anyString(), any(), any(), any(), any(), any()))
                .thenReturn(1);


        assertThatThrownBy(() -> publicationService.updateUserPublication(testPublicationId, updateDTO, authentication))
                .isInstanceOf(PublicationNotFoundException.class);
    }

    @Test
    void deletePublication_WithValidId_ShouldReturnNoContent() {

        when(publicationRepository.findById(testPublicationId)).thenReturn(Optional.of(testPublication));


        ResponseEntity<Object> result = publicationService.deletePublication(testPublicationId, authentication);


        assertThat(result.getStatusCode().value()).isEqualTo(204);
        verify(publicationAttributeValueService).deletePublicationAttributes(testPublicationId);
        verify(productFileService).deletePublicationFiles(testPublicationId);
        verify(publicationDocumentService).deleteById(testPublicationId);
        verify(numericValueService).deletePublicationNumericFields(testPublicationId);
        verify(publicationLikeService).deletePublicationLikes(testPublicationId);
        verify(publicationViewService).deletePublicationViews(testPublicationId);
        verify(publicationRepository).deleteById(testPublicationId);
    }

    @Test
    void deletePublication_PublicationNotFound_ShouldReturnNotFound() {

        when(publicationRepository.findById(testPublicationId)).thenReturn(Optional.empty());


        ResponseEntity<Object> result = publicationService.deletePublication(testPublicationId, authentication);


        assertThat(result.getStatusCode().value()).isEqualTo(404);
        verify(publicationRepository, never()).deleteById(any());
    }

    @Test
    void getPublicationResponseDTOS_ShouldMapCorrectly() {

        Page<Publication> page = new PageImpl<>(List.of(testPublication));

        mockPublicationResponseMapping();

        PublicationResponseDTO responseDTO = PublicationResponseDTO.builder()
                .id(testPublicationId)
                .build();
        when(publicationMapper.toPublicationResponseDTO(any(), anyList(), any(), anyList(), anyBoolean(), anyBoolean(), anyList()))
                .thenReturn(responseDTO);


        List<PublicationResponseDTO> result = publicationService.getPublicationResponseDTOS(page, testUser);


        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(testPublicationId);
        verify(publicationViewService).views(testPublicationId);
        verify(publicationViewService).isViewed(testUser.getUserId(), testPublicationId);
    }

    @Test
    void savePublication_WithNullVideoUrl_ShouldNotSaveVideo() {

        publicationRequestDTO.setVideoUrl(null);

        when(authentication.getPrincipal()).thenReturn(testUser);
        when(locationService.getLocation(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(locationDTO);
        when(publicationMapper.toPublication(any(), any(), any())).thenReturn(testPublication);
        when(publicationRepository.save(any(Publication.class))).thenReturn(testPublication);
        when(numericValueService.savePublicationNumericValues(anyList(), any()))
                .thenReturn(Collections.emptyList());
        when(publicationAttributeValueService.savePublicationAttributeValues(anyList(), any(), anyList()))
                .thenReturn(Collections.emptyList());


        publicationService.savePublication(publicationRequestDTO, authentication);


        verify(productFileService, never()).saveVideo(anyString(), any());
    }

    @Test
    void savePublication_WithNullProductVariants_ShouldNotSaveVariants() {

        publicationRequestDTO.setProductVariants(null);

        when(authentication.getPrincipal()).thenReturn(testUser);
        when(locationService.getLocation(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(locationDTO);
        when(publicationMapper.toPublication(any(), any(), any())).thenReturn(testPublication);
        when(publicationRepository.save(any(Publication.class))).thenReturn(testPublication);
        when(numericValueService.savePublicationNumericValues(anyList(), any()))
                .thenReturn(Collections.emptyList());
        when(publicationAttributeValueService.savePublicationAttributeValues(anyList(), any(), anyList()))
                .thenReturn(Collections.emptyList());


        publicationService.savePublication(publicationRequestDTO, authentication);


        verify(productVariantService, never()).save(any(), any());
    }

    @Test
    void savePublication_WithEmptyProductVariants_ShouldNotSaveVariants() {

        publicationRequestDTO.setProductVariants(Collections.emptyList());

        when(authentication.getPrincipal()).thenReturn(testUser);
        when(locationService.getLocation(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(locationDTO);
        when(publicationMapper.toPublication(any(), any(), any())).thenReturn(testPublication);
        when(publicationRepository.save(any(Publication.class))).thenReturn(testPublication);
        when(numericValueService.savePublicationNumericValues(anyList(), any()))
                .thenReturn(Collections.emptyList());
        when(publicationAttributeValueService.savePublicationAttributeValues(anyList(), any(), anyList()))
                .thenReturn(Collections.emptyList());


        publicationService.savePublication(publicationRequestDTO, authentication);


        verify(productVariantService, never()).save(any(), any());
    }

    @Test
    void updateUserPublication_WithProductVariants_ShouldUpdateVariants() {

        ProductVariantResponseDTO updatedVariant = ProductVariantResponseDTO.builder().size("Updated Variant")
                .build();

        UpdatePublicationRequestDTO updateDTO = UpdatePublicationRequestDTO.builder()
                .title("Updated Title")
                .description("Updated Description")
                .price(200.0f)
                .productVariants(Collections.singletonList(updatedVariant))
                .build();

        ProductVariant variant = ProductVariant.builder()
                .size("Updated Variant")
                .build();

        when(authentication.getPrincipal()).thenReturn(testUser);
        when(publicationRepository.findById(testPublicationId)).thenReturn(Optional.of(testPublication));
        when(publicationRepository.updatePublicationById(any(), anyString(), anyString(), any(), any(), any(), any(), any()))
                .thenReturn(1);
        when(productVariantService.update(any(), any(), any())).thenReturn(variant);

        mockPublicationResponseMapping();

        PublicationResponseDTO responseDTO = PublicationResponseDTO.builder().id(testPublicationId).build();
        when(publicationMapper.toPublicationResponseDTO(any(), anyList(), any(), anyList(), anyBoolean(), anyBoolean(), anyList()))
                .thenReturn(responseDTO);


        PublicationResponseDTO result = publicationService.updateUserPublication(testPublicationId, updateDTO, authentication);


        assertThat(result).isNotNull();
    }

    private void mockPublicationResponseMapping() {
        when(productFileService.findImagesByPublicationId(any())).thenReturn(Collections.emptyList());
        when(productFileService.findVideoUrlByPublicationId(any())).thenReturn(null);
        when(numericValueService.findNumericFields(any())).thenReturn(Collections.emptyList());
        when(userService.isFollowingToUser(any(), any())).thenReturn(false);
        when(publicationViewService.views(any())).thenReturn(0L);
        when(publicationViewService.isViewed(any(), any())).thenReturn(false);

        PublicationResponseDTO responseDTO = PublicationResponseDTO.builder().id(testPublicationId).build();
        when(publicationMapper.toPublicationResponseDTO(any(), anyList(), any(), anyList(), anyBoolean(), anyBoolean(), anyList()))
                .thenReturn(responseDTO);
    }
}