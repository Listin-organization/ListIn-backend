package com.igriss.ListIn.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import com.igriss.ListIn.exceptions.SearchQueryException;
import com.igriss.ListIn.publication.dto.PublicationResponseDTO;
import com.igriss.ListIn.publication.dto.page.PageResponse;
import com.igriss.ListIn.publication.entity.Publication;
import com.igriss.ListIn.publication.mapper.PublicationMapper;
import com.igriss.ListIn.publication.repository.PublicationRepository;
import com.igriss.ListIn.publication.service.NumericValueService;
import com.igriss.ListIn.publication.service.ProductFileService;
import com.igriss.ListIn.publication.service.PublicationLikeService;
import com.igriss.ListIn.publication.service.PublicationViewService;
import com.igriss.ListIn.search.document.PublicationDocument;
import com.igriss.ListIn.search.dto.FoundPublicationsDTO;
import com.igriss.ListIn.search.mapper.SearchParamMapper;
import com.igriss.ListIn.search.service.supplier.SearchParams;
import com.igriss.ListIn.user.entity.User;
import com.igriss.ListIn.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PublicationSearchServiceTest {

    @InjectMocks
    private PublicationSearchServiceImpl service;

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @Mock
    private SearchParamMapper searchParamMapper;

    @Mock
    private PublicationMapper publicationMapper;

    @Mock
    private ProductFileService productFileService;

    @Mock
    private PublicationRepository publicationRepository;

    @Mock
    private PublicationLikeService publicationLikeService;

    @Mock
    private PublicationViewService publicationViewService;

    @Mock
    private NumericValueService numericValueService;

    @Mock
    private UserService userService;

    @Mock
    private RedisTemplate<UUID, String> redisTemplate;

    @Mock
    private ListOperations<UUID, String> listOperations;

    @Mock
    private Authentication authentication;

    @Mock
    private User user;

    private UUID userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        when(authentication.getPrincipal()).thenReturn(user);
        when(user.getUserId()).thenReturn(userId);

        when(redisTemplate.opsForList()).thenReturn(listOperations);

        ReflectionTestUtils.setField(service, "indexName", "publications");
        ReflectionTestUtils.setField(service, "redisHistoryTemplate", redisTemplate);
    }

    @Test
    void searchWithAdvancedFilter_elasticsearchException() throws IOException {
        when(searchParamMapper.toSearchParams(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any())).thenReturn(mock(SearchParams.class));

        when(elasticsearchClient.search(any(Function.class), eq(PublicationDocument.class)))
                .thenThrow(new IOException("ES failed"));

        assertThrows(SearchQueryException.class, () -> service.searchWithAdvancedFilter(
                null, null, null, 0, 10, null, null,
                null, null, null, null, null, null, null,
                null, null, authentication
        ));
    }

    @Test
    void searchWithAdvancedFilter_success() throws IOException, SearchQueryException {
        SearchParams searchParams = mock(SearchParams.class);
        when(searchParamMapper.toSearchParams(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any())).thenReturn(searchParams);

        PublicationDocument publicationDocument = new PublicationDocument();
        UUID publicationId = UUID.randomUUID();
        publicationDocument.setId(publicationId);

        SearchResponse<PublicationDocument> searchResponse = mock(SearchResponse.class);
        HitsMetadata<PublicationDocument> hitsMetadata = mock(HitsMetadata.class);
        TotalHits totalHits = mock(TotalHits.class);
        when(totalHits.value()).thenReturn(1L);
        when(hitsMetadata.total()).thenReturn(totalHits);

        List<Hit<PublicationDocument>> hits = new ArrayList<>();
        Hit<PublicationDocument> hit = mock(Hit.class);
        when(hit.source()).thenReturn(publicationDocument);
        hits.add(hit);

        when(hitsMetadata.hits()).thenReturn(hits);
        when(searchResponse.hits()).thenReturn(hitsMetadata);

        when(elasticsearchClient.search(any(Function.class), eq(PublicationDocument.class)))
                .thenReturn(searchResponse);

        Publication publication = mock(Publication.class);
        when(publication.getId()).thenReturn(publicationId);
        User seller = mock(User.class);
        UUID sellerId = UUID.randomUUID();
        when(seller.getUserId()).thenReturn(sellerId);
        when(publication.getSeller()).thenReturn(seller);

        when(publicationRepository.findAllByIdInOrderByDatePosted(anyList())).thenReturn(List.of(publication));

        PublicationResponseDTO dto = mock(PublicationResponseDTO.class);
        when(publicationMapper.toPublicationResponseDTO(any(), any(), any(), any(), anyBoolean(), anyBoolean(), any())).thenReturn(dto);

        PageResponse<PublicationResponseDTO> result = service.searchWithAdvancedFilter(
                UUID.randomUUID(), UUID.randomUUID(), "test query", 0, 10, true, "NEW",
                10.0f, 100.0f, "New York", false, "INDIVIDUAL",
                "US.NY.Kings", "search text", List.of("brand:Apple"), List.of("price:10~100"), authentication
        );

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getNumber());
        assertEquals(10, result.getSize());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());

        verify(listOperations).leftPush(userId, "search text");
        verify(listOperations).trim(userId, 0, 9);
    }

    @Test
    void searchWithAdvancedFilter_nullSearchText() throws IOException, SearchQueryException {
        SearchParams searchParams = mock(SearchParams.class);
        when(searchParamMapper.toSearchParams(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any())).thenReturn(searchParams);

        PublicationDocument publicationDocument = new PublicationDocument();
        UUID publicationId = UUID.randomUUID();
        publicationDocument.setId(publicationId);

        SearchResponse<PublicationDocument> searchResponse = mock(SearchResponse.class);
        HitsMetadata<PublicationDocument> hitsMetadata = mock(HitsMetadata.class);
        TotalHits totalHits = mock(TotalHits.class);
        when(totalHits.value()).thenReturn(1L);
        when(hitsMetadata.total()).thenReturn(totalHits);

        List<Hit<PublicationDocument>> hits = new ArrayList<>();
        Hit<PublicationDocument> hit = mock(Hit.class);
        when(hit.source()).thenReturn(publicationDocument);
        hits.add(hit);

        when(hitsMetadata.hits()).thenReturn(hits);
        when(searchResponse.hits()).thenReturn(hitsMetadata);

        when(elasticsearchClient.search(any(Function.class), eq(PublicationDocument.class)))
                .thenReturn(searchResponse);

        Publication publication = mock(Publication.class);
        when(publication.getId()).thenReturn(publicationId);
        User seller = mock(User.class);
        UUID sellerId = UUID.randomUUID();
        when(seller.getUserId()).thenReturn(sellerId);
        when(publication.getSeller()).thenReturn(seller);

        when(publicationRepository.findAllByIdInOrderByDatePosted(anyList())).thenReturn(List.of(publication));

        PublicationResponseDTO dto = mock(PublicationResponseDTO.class);
        when(publicationMapper.toPublicationResponseDTO(any(), any(), any(), any(), anyBoolean(), anyBoolean(), any())).thenReturn(dto);

        PageResponse<PublicationResponseDTO> result = service.searchWithAdvancedFilter(
                UUID.randomUUID(), UUID.randomUUID(), "test query", 0, 10, true, "NEW",
                10.0f, 100.0f, "New York", false, "INDIVIDUAL",
                "US.NY.Kings", null, List.of("brand:Apple"), List.of("price:10~100"), authentication
        );

        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(listOperations, never()).leftPush(any(), any());
        verify(listOperations, never()).trim(any(), anyLong(), anyLong());
    }

    @Test
    void findAllLatestPublications_success() {
        Publication publication = mock(Publication.class);
        User seller = mock(User.class);
        UUID sellerId = UUID.randomUUID();
        when(seller.getUserId()).thenReturn(sellerId);
        when(publication.getSeller()).thenReturn(seller);

        Page<Publication> page = new PageImpl<>(List.of(publication));
        when(publicationRepository.findAllByOrderByDatePostedDesc(any(Pageable.class))).thenReturn(page);

        PublicationResponseDTO dto = mock(PublicationResponseDTO.class);
        when(publicationMapper.toPublicationResponseDTO(any(), any(), any(), any(), anyBoolean(), anyBoolean(), any())).thenReturn(dto);
        when(productFileService.findImagesByPublicationId(any())).thenReturn(List.of());
        when(productFileService.findVideoUrlByPublicationId(any())).thenReturn(null);
        when(numericValueService.findNumericFields(any())).thenReturn(List.of());
        when(publicationLikeService.isLiked(any(), any())).thenReturn(false);
        when(userService.isFollowingToUser(any(), any())).thenReturn(true);
        when(publicationViewService.views(any())).thenReturn(0L);
        when(publicationViewService.isViewed(any(), any())).thenReturn(false);

        PageResponse<PublicationResponseDTO> response = service.findAllLatestPublications(0, 10, null, null,
                null, null, null, null, null, authentication);


        assertThat(response.getContent()).hasSize(1);
        verify(publicationRepository).findAllByOrderByDatePostedDesc(any(Pageable.class));
    }

    @Test
    void getPublicationsCount_success() throws IOException, SearchQueryException {
        SearchParams searchParams = mock(SearchParams.class);
        when(searchParamMapper.toSearchParams(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any())).thenReturn(searchParams);

        PublicationDocument doc1 = new PublicationDocument();
        doc1.setPrice(50.0f);
        PublicationDocument doc2 = new PublicationDocument();
        doc2.setPrice(100.0f);

        SearchResponse<PublicationDocument> searchResponse = mock(SearchResponse.class);
        HitsMetadata<PublicationDocument> hitsMetadata = mock(HitsMetadata.class);
        TotalHits totalHits = mock(TotalHits.class);
        when(totalHits.value()).thenReturn(2L);
        when(hitsMetadata.total()).thenReturn(totalHits);

        List<Hit<PublicationDocument>> hits = new ArrayList<>();
        Hit<PublicationDocument> hit1 = mock(Hit.class);
        when(hit1.source()).thenReturn(doc1);
        Hit<PublicationDocument> hit2 = mock(Hit.class);
        when(hit2.source()).thenReturn(doc2);
        hits.add(hit1);
        hits.add(hit2);

        when(hitsMetadata.hits()).thenReturn(hits);
        when(searchResponse.hits()).thenReturn(hitsMetadata);

        when(elasticsearchClient.search(any(Function.class), eq(PublicationDocument.class)))
                .thenReturn(searchResponse);

        FoundPublicationsDTO result = service.getPublicationsCount(
                UUID.randomUUID(), UUID.randomUUID(), "test query", 0, 10, true, "NEW",
                10.0f, 100.0f, "New York", false, "INDIVIDUAL",
                "US.NY.Kings", List.of("brand:Apple"), List.of("price:10~100")
        );

        assertNotNull(result);
        assertEquals(2L, result.getFoundPublications());
        assertEquals(50.0f, result.getPriceFrom());
        assertEquals(100.0f, result.getPriceTo());
    }

    @Test
    void getPublicationsCount_elasticsearchException() throws IOException {
        when(searchParamMapper.toSearchParams(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any())).thenReturn(mock(SearchParams.class));

        when(elasticsearchClient.search(any(Function.class), eq(PublicationDocument.class)))
                .thenThrow(new IOException("ES failed"));

        assertThrows(SearchQueryException.class, () -> service.getPublicationsCount(
                null, null, null, 0, 10, null, null,
                null, null, null, null, null,
                null, null, null
        ));
    }

    @Test
    void getLastQueriedValues_success() {
        when(listOperations.range(userId, 0, -1)).thenReturn(List.of("query1", "query2"));

        List<String> result = service.getLastQueriedValues(authentication);

        assertThat(result).containsExactly("query1", "query2");
        verify(listOperations).range(userId, 0, -1);
    }

    @Test
    void testParseLocations() throws SearchQueryException, IOException {

        SearchParams searchParams = mock(SearchParams.class);
        when(searchParamMapper.toSearchParams(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any())).thenReturn(searchParams);

        PublicationDocument publicationDocument = new PublicationDocument();
        UUID publicationId = UUID.randomUUID();
        publicationDocument.setId(publicationId);

        SearchResponse<PublicationDocument> searchResponse = mock(SearchResponse.class);
        HitsMetadata<PublicationDocument> hitsMetadata = mock(HitsMetadata.class);
        TotalHits totalHits = mock(TotalHits.class);
        when(totalHits.value()).thenReturn(1L);
        when(hitsMetadata.total()).thenReturn(totalHits);

        List<Hit<PublicationDocument>> hits = new ArrayList<>();
        Hit<PublicationDocument> hit = mock(Hit.class);
        when(hit.source()).thenReturn(publicationDocument);
        hits.add(hit);

        when(hitsMetadata.hits()).thenReturn(hits);
        when(searchResponse.hits()).thenReturn(hitsMetadata);

        when(elasticsearchClient.search(any(Function.class), eq(PublicationDocument.class)))
                .thenReturn(searchResponse);

        Publication publication = mock(Publication.class);
        when(publication.getId()).thenReturn(publicationId);
        User seller = mock(User.class);
        when(seller.getUserId()).thenReturn(UUID.randomUUID());
        when(publication.getSeller()).thenReturn(seller);

        when(publicationRepository.findAllByIdInOrderByDatePosted(anyList())).thenReturn(List.of(publication));

        PublicationResponseDTO dto = mock(PublicationResponseDTO.class);
        when(publicationMapper.toPublicationResponseDTO(any(), any(), any(), any(), anyBoolean(), anyBoolean(), any())).thenReturn(dto);

        service.searchWithAdvancedFilter(
                null, null, null, 0, 10, null, null,
                null, null, null, null, null,
                "US", null, null, null, authentication
        );

        ArgumentCaptor<Map<String, String>> locationCaptor = ArgumentCaptor.forClass(Map.class);
        verify(searchParamMapper, atLeastOnce()).toSearchParams(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), locationCaptor.capture(), any(), any(), any());

        Map<String, String> capturedLocations = locationCaptor.getValue();
        assertNotNull(capturedLocations);
        assertEquals("US", capturedLocations.get("countryId"));

        reset(searchParamMapper);
        when(searchParamMapper.toSearchParams(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any())).thenReturn(searchParams);

        service.searchWithAdvancedFilter(
                null, null, null, 0, 10, null, null,
                null, null, null, null, null,
                "US.NY", null, null, null, authentication
        );

        verify(searchParamMapper, atLeastOnce()).toSearchParams(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), locationCaptor.capture(), any(), any(), any());

        capturedLocations = locationCaptor.getValue();
        assertNotNull(capturedLocations);
        assertEquals("US", capturedLocations.get("countryId"));
        assertEquals("NY", capturedLocations.get("stateId"));

        reset(searchParamMapper);
        when(searchParamMapper.toSearchParams(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any())).thenReturn(searchParams);

        service.searchWithAdvancedFilter(
                null, null, null, 0, 10, null, null,
                null, null, null, null, null,
                "US.NY.Kings", null, null, null, authentication
        );

        verify(searchParamMapper, atLeastOnce()).toSearchParams(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), locationCaptor.capture(), any(), any(), any());

        capturedLocations = locationCaptor.getValue();
        assertNotNull(capturedLocations);
        assertEquals("US", capturedLocations.get("countryId"));
        assertEquals("NY", capturedLocations.get("stateId"));
        assertEquals("Kings", capturedLocations.get("countyId"));

        reset(searchParamMapper);
        when(searchParamMapper.toSearchParams(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any())).thenReturn(searchParams);

        service.searchWithAdvancedFilter(
                null, null, null, 0, 10, null, null,
                null, null, null, null, null,
                "US.NY.Kings.Brooklyn", null, null, null, authentication
        );

        verify(searchParamMapper, atLeastOnce()).toSearchParams(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), locationCaptor.capture(), any(), any(), any());

        capturedLocations = locationCaptor.getValue();
        assertNotNull(capturedLocations);
        assertEquals("US", capturedLocations.get("countryId"));
        assertEquals("NY", capturedLocations.get("stateId"));
        assertEquals("Kings", capturedLocations.get("countyId"));
        assertEquals("Brooklyn", capturedLocations.get("cityId"));
    }

    @Test
    void testParseAttributeFilter() throws SearchQueryException, IOException {
        SearchParams searchParams = mock(SearchParams.class);
        when(searchParamMapper.toSearchParams(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any())).thenReturn(searchParams);

        PublicationDocument publicationDocument = new PublicationDocument();
        UUID publicationId = UUID.randomUUID();
        publicationDocument.setId(publicationId);

        SearchResponse<PublicationDocument> searchResponse = mock(SearchResponse.class);
        HitsMetadata<PublicationDocument> hitsMetadata = mock(HitsMetadata.class);
        TotalHits totalHits = mock(TotalHits.class);
        when(totalHits.value()).thenReturn(1L);
        when(hitsMetadata.total()).thenReturn(totalHits);

        List<Hit<PublicationDocument>> hits = new ArrayList<>();
        Hit<PublicationDocument> hit = mock(Hit.class);
        when(hit.source()).thenReturn(publicationDocument);
        hits.add(hit);

        when(hitsMetadata.hits()).thenReturn(hits);
        when(searchResponse.hits()).thenReturn(hitsMetadata);

        when(elasticsearchClient.search(any(Function.class), eq(PublicationDocument.class)))
                .thenReturn(searchResponse);

        Publication publication = mock(Publication.class);
        when(publication.getId()).thenReturn(publicationId);
        User seller = mock(User.class);
        when(seller.getUserId()).thenReturn(UUID.randomUUID());
        when(publication.getSeller()).thenReturn(seller);

        when(publicationRepository.findAllByIdInOrderByDatePosted(anyList())).thenReturn(List.of(publication));

        PublicationResponseDTO dto = mock(PublicationResponseDTO.class);
        when(publicationMapper.toPublicationResponseDTO(any(), any(), any(), any(), anyBoolean(), anyBoolean(), any())).thenReturn(dto);

        service.searchWithAdvancedFilter(
                null, null, null, 0, 10, null, null,
                null, null, null, null, null,
                null, null, List.of("brand:Apple"), null, authentication
        );

        ArgumentCaptor<Map<String, List<String>>> attributeCaptor = ArgumentCaptor.forClass(Map.class);
        verify(searchParamMapper, atLeastOnce()).toSearchParams(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), attributeCaptor.capture(), any());

        Map<String, List<String>> capturedAttributes = attributeCaptor.getValue();
        assertNotNull(capturedAttributes);
        assertTrue(capturedAttributes.containsKey("brand"));
        assertEquals(List.of("Apple"), capturedAttributes.get("brand"));

        reset(searchParamMapper);
        when(searchParamMapper.toSearchParams(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any())).thenReturn(searchParams);

        service.searchWithAdvancedFilter(
                null, null, null, 0, 10, null, null,
                null, null, null, null, null,
                null, null, List.of("brand:Apple", "color:Red"), null, authentication
        );

        verify(searchParamMapper, atLeastOnce()).toSearchParams(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), attributeCaptor.capture(), any());

        capturedAttributes = attributeCaptor.getValue();
        assertNotNull(capturedAttributes);
        assertTrue(capturedAttributes.containsKey("brand"));
        assertTrue(capturedAttributes.containsKey("color"));
        assertEquals(List.of("Apple"), capturedAttributes.get("brand"));
        assertEquals(List.of("Red"), capturedAttributes.get("color"));
    }

    @Test
    void testParseNumericFilter() throws SearchQueryException, IOException {
        SearchParams searchParams = mock(SearchParams.class);
        when(searchParamMapper.toSearchParams(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any())).thenReturn(searchParams);

        PublicationDocument publicationDocument = new PublicationDocument();
        UUID publicationId = UUID.randomUUID();
        publicationDocument.setId(publicationId);

        SearchResponse<PublicationDocument> searchResponse = mock(SearchResponse.class);
        HitsMetadata<PublicationDocument> hitsMetadata = mock(HitsMetadata.class);
        TotalHits totalHits = mock(TotalHits.class);
        when(totalHits.value()).thenReturn(1L);
        when(hitsMetadata.total()).thenReturn(totalHits);

        List<Hit<PublicationDocument>> hits = new ArrayList<>();
        Hit<PublicationDocument> hit = mock(Hit.class);
        when(hit.source()).thenReturn(publicationDocument);
        hits.add(hit);

        when(hitsMetadata.hits()).thenReturn(hits);
        when(searchResponse.hits()).thenReturn(hitsMetadata);

        when(elasticsearchClient.search(any(Function.class), eq(PublicationDocument.class)))
                .thenReturn(searchResponse);

        Publication publication = mock(Publication.class);
        when(publication.getId()).thenReturn(publicationId);
        User seller = mock(User.class);
        when(seller.getUserId()).thenReturn(UUID.randomUUID());
        when(publication.getSeller()).thenReturn(seller);

        when(publicationRepository.findAllByIdInOrderByDatePosted(anyList())).thenReturn(List.of(publication));

        PublicationResponseDTO dto = mock(PublicationResponseDTO.class);
        when(publicationMapper.toPublicationResponseDTO(any(), any(), any(), any(), anyBoolean(), anyBoolean(), any())).thenReturn(dto);

        service.searchWithAdvancedFilter(
                null, null, null, 0, 10, null, null,
                null, null, null, null, null,
                null, null, null, List.of("price:10~100"), authentication
        );

        ArgumentCaptor<Map<String, String[]>> numericCaptor = ArgumentCaptor.forClass(Map.class);
        verify(searchParamMapper, atLeastOnce()).toSearchParams(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), numericCaptor.capture());

        Map<String, String[]> capturedNumeric = numericCaptor.getValue();
        assertNotNull(capturedNumeric);
        assertTrue(capturedNumeric.containsKey("price"));
        assertArrayEquals(new String[]{"10", "100"}, capturedNumeric.get("price"));

        reset(searchParamMapper);
        when(searchParamMapper.toSearchParams(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any())).thenReturn(searchParams);

        service.searchWithAdvancedFilter(
                null, null, null, 0, 10, null, null,
                null, null, null, null, null,
                null, null, null, List.of("price:10~100", "weight:5~10"), authentication
        );

        verify(searchParamMapper, atLeastOnce()).toSearchParams(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), numericCaptor.capture());

        capturedNumeric = numericCaptor.getValue();
        assertNotNull(capturedNumeric);
        assertTrue(capturedNumeric.containsKey("price"));
        assertTrue(capturedNumeric.containsKey("weight"));
        assertArrayEquals(new String[]{"10", "100"}, capturedNumeric.get("price"));
        assertArrayEquals(new String[]{"5", "10"}, capturedNumeric.get("weight"));
    }
}
