package com.igriss.ListIn.publication.service;

import com.igriss.ListIn.config.Images.S3Service;
import com.igriss.ListIn.exceptions.InvalidUrlException;
import com.igriss.ListIn.publication.entity.Publication;
import com.igriss.ListIn.publication.entity.PublicationImage;
import com.igriss.ListIn.publication.entity.PublicationVideo;
import com.igriss.ListIn.publication.mapper.PublicationImageMapper;
import com.igriss.ListIn.publication.mapper.PublicationVideoMapper;
import com.igriss.ListIn.publication.repository.ProductImageRepository;
import com.igriss.ListIn.publication.repository.ProductVideoRepository;
import com.igriss.ListIn.publication.service_impl.ProductFileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class ProductFileServiceTest {

    @Mock
    private PublicationImageMapper publicationImageMapper;
    @Mock
    private PublicationVideoMapper publicationVideoMapper;
    @Mock
    private ProductImageRepository productImageRepository;
    @Mock
    private ProductVideoRepository productVideoRepository;
    @Mock
    private S3Service s3Service;

    @InjectMocks
    private ProductFileServiceImpl productFileService;

    private Publication publication;
    private UUID publicationId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        publicationId = UUID.randomUUID();
        publication = Publication.builder().id(publicationId).build();
    }

    @Test
    void saveImages_shouldMapAndSaveAllImages() {
        List<String> urls = List.of("url1", "url2");
        List<PublicationImage> mappedImages = urls.stream()
                .map(url -> PublicationImage.builder().imageUrl(url).publication(publication).build())
                .toList();

        when(publicationImageMapper.toProductImage(anyString(), eq(publication)))
                .thenAnswer(invocation -> PublicationImage.builder()
                        .imageUrl(invocation.getArgument(0))
                        .publication(publication)
                        .build());

        when(productImageRepository.saveAll(anyList())).thenReturn(mappedImages);

        productFileService.saveImages(urls, publication);

        verify(productImageRepository, times(1)).saveAll(anyList());
    }

    @Test
    void saveVideo_shouldMapAndSaveVideo() {
        String url = "video.mp4";
        PublicationVideo video = PublicationVideo.builder().videoUrl(url).publication(publication).build();

        when(publicationVideoMapper.toProductVideo(url, publication)).thenReturn(video);

        productFileService.saveVideo(url, publication);

        verify(productVideoRepository, times(1)).save(video);
    }

    @Test
    void uploadImageURLs_shouldReturnUploadedUrls() {
        MultipartFile file = mock(MultipartFile.class);
        when(s3Service.uploadFile(anyList())).thenReturn(List.of("uploaded-url"));

        List<String> result = productFileService.uploadImageURLs(List.of(file));

        assertEquals(1, result.size());
        assertEquals("uploaded-url", result.get(0));
    }

    @Test
    void uploadVideoURL_shouldReturnSingleUploadedUrl() {
        MultipartFile file = mock(MultipartFile.class);
        when(s3Service.uploadFile(anyList())).thenReturn(List.of("video-url"));

        String result = productFileService.uploadVideoURL(file);

        assertEquals("video-url", result);
    }

    @Test
    void findImagesByPublicationId_shouldReturnImages() {
        List<PublicationImage> list = List.of(PublicationImage.builder().imageUrl("img1").build());
        when(productImageRepository.findAllByPublication_Id(publicationId)).thenReturn(list);

        List<PublicationImage> result = productFileService.findImagesByPublicationId(publicationId);

        assertEquals(1, result.size());
    }

    @Test
    void findVideoUrlByPublicationId_shouldReturnVideoUrl() {
        PublicationVideo video = PublicationVideo.builder().videoUrl("video.mp4").build();
        when(productVideoRepository.findByPublication_Id(publicationId)).thenReturn(Optional.of(video));

        String result = productFileService.findVideoUrlByPublicationId(publicationId);

        assertEquals("video.mp4", result);
    }

    @Test
    void updateImagesByPublication_shouldSaveAndDeleteCorrectly() {
        Map<Boolean, List<String>> urls = new HashMap<>();
        urls.put(true, List.of("newImage"));
        urls.put(false, List.of("oldImage"));

        doNothing().when(productImageRepository).deleteAllByPublication_IdAndImageUrlIn(any(), anyList());
        doNothing().when(s3Service).deleteFiles(anyList());
        when(publicationImageMapper.toProductImage(anyString(), any())).thenReturn(PublicationImage.builder().build());
        when(productImageRepository.saveAll(anyList())).thenReturn(List.of());

        productFileService.updateImagesByPublication(publication, urls);

        verify(productImageRepository, times(1)).deleteAllByPublication_IdAndImageUrlIn(any(), anyList());
        verify(s3Service, times(1)).deleteFiles(anyList());
    }

    @Test
    void updateVideoByPublication_shouldReplaceVideo() {
        PublicationVideo existingVideo = PublicationVideo.builder().videoUrl("old.mp4").build();
        when(productVideoRepository.findByPublication_Id(publicationId)).thenReturn(Optional.of(existingVideo));

        Map<Boolean, String> videoMap = Map.of(
                false, "old.mp4",
                true, "new.mp4"
        );

        doNothing().when(s3Service).deleteFiles(anyList());

        when(publicationVideoMapper.toProductVideo(anyString(), any())).thenReturn(
                PublicationVideo.builder().videoUrl("new.mp4").build()
        );

        productFileService.updateVideoByPublication(publication, videoMap);

        verify(productVideoRepository, times(1)).deleteByPublication_Id(publicationId);
        verify(productVideoRepository, times(1)).save(any(PublicationVideo.class));
    }

    @Test
    void updateVideoByPublication_ShouldSaveVideo_WhenNoExistingVideoAndTrueKeyPresent() {
        // Arrange
        String newVideoUrl = "video-new.mp4";
        Map<Boolean, String> videoUrl = Map.of(true, newVideoUrl);

        when(productVideoRepository.findByPublication_Id(publication.getId()))
                .thenReturn(Optional.empty());

        PublicationVideo mappedVideo = new PublicationVideo();
        when(publicationVideoMapper.toProductVideo(eq(newVideoUrl), eq(publication)))
                .thenReturn(mappedVideo);

        // Act
        productFileService.updateVideoByPublication(publication, videoUrl);

        // Assert
        verify(productVideoRepository, times(1))
                .findByPublication_Id(publication.getId());
        verify(publicationVideoMapper, times(1))
                .toProductVideo(eq(newVideoUrl), eq(publication));
        verify(productVideoRepository, times(1))
                .save(eq(mappedVideo));
        verifyNoMoreInteractions(productVideoRepository);
        verifyNoInteractions(s3Service);
    }

    @Test
    void deletePublicationFiles_shouldDeleteImagesAndVideos() {
        PublicationImage img = PublicationImage.builder().imageId(UUID.randomUUID()).imageUrl("image1.jpg").build();
        PublicationVideo vid = PublicationVideo.builder().videoId(UUID.randomUUID()).videoUrl("video1.mp4").build();

        when(productImageRepository.findAllByPublication_Id(publicationId)).thenReturn(List.of(img));
        when(productVideoRepository.findByPublication_Id(publicationId)).thenReturn(Optional.of(vid));

        doNothing().when(s3Service).deleteFiles(anyList());

        productFileService.deletePublicationFiles(publicationId);

        verify(productImageRepository, times(1)).deleteById(img.getImageId());
        verify(productVideoRepository, times(1)).deleteById(vid.getVideoId());
        verify(s3Service, atLeastOnce()).deleteFiles(anyList());
    }

    @Test
    void deletePublicationFiles_ThrowException() {
        PublicationImage img = PublicationImage.builder().imageId(UUID.randomUUID()).imageUrl(":https://image1.jpg").build();
        PublicationVideo vid = PublicationVideo.builder().videoId(UUID.randomUUID()).videoUrl(":https://video1.mp4").build();

        when(productImageRepository.findAllByPublication_Id(publicationId)).thenReturn(List.of(img));
        when(productVideoRepository.findByPublication_Id(publicationId)).thenReturn(Optional.of(vid));

        doNothing().when(s3Service).deleteFiles(anyList());

        assertThrows(InvalidUrlException.class, () -> productFileService.deletePublicationFiles(publicationId));
    }

    @Test
    void getVideoPublicationsByParent_ShouldReturnPageOfVideos() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        UUID parentCategoryId = UUID.randomUUID();

        PublicationVideo video1 = new PublicationVideo();
        PublicationVideo video2 = new PublicationVideo();
        Page<PublicationVideo> mockPage = new PageImpl<>(List.of(video1, video2));

        when(productVideoRepository
                .findAllByPublication_Category_ParentCategory_IdOrderByPublication_DateUpdatedDesc(
                        parentCategoryId, pageRequest))
                .thenReturn(mockPage);

        Page<PublicationVideo> result =
                productFileService.getVideoPublicationsByParent(parentCategoryId, pageRequest);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).containsExactly(video1, video2);

        verify(productVideoRepository, times(1))
                .findAllByPublication_Category_ParentCategory_IdOrderByPublication_DateUpdatedDesc(
                        parentCategoryId, pageRequest);
        verifyNoMoreInteractions(productVideoRepository);
    }

    @Test
    void getVideoPublications_ShouldReturnPageOfVideos() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        PublicationVideo video1 = new PublicationVideo();
        Page<PublicationVideo> mockPage = new PageImpl<>(List.of(video1));

        when(productVideoRepository.findAllByOrderByPublication_DateUpdatedDesc(pageRequest))
                .thenReturn(mockPage);

        Page<PublicationVideo> result = productFileService.getVideoPublications(pageRequest);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(video1);

        verify(productVideoRepository, times(1))
                .findAllByOrderByPublication_DateUpdatedDesc(pageRequest);
        verifyNoMoreInteractions(productVideoRepository);
    }

}
