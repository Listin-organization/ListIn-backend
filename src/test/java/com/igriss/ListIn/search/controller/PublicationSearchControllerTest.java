package com.igriss.ListIn.search.controller;

import com.igriss.ListIn.exceptions.SearchQueryException;
import com.igriss.ListIn.publication.dto.PublicationResponseDTO;
import com.igriss.ListIn.publication.dto.page.PageResponse;
import com.igriss.ListIn.search.dto.FoundPublicationsDTO;
import com.igriss.ListIn.search.dto.InputPredictionResponseDTO;
import com.igriss.ListIn.search.service.InputPredictionService;
import com.igriss.ListIn.search.service.PublicationSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PublicationSearchControllerTest {

    private PublicationSearchService searchService;
    private InputPredictionService inputPredictionService;
    private PublicationSearchController controller;

    @BeforeEach
    void setUp() {
        searchService = mock(PublicationSearchService.class);
        inputPredictionService = mock(InputPredictionService.class);
        controller = new PublicationSearchController(searchService, inputPredictionService);
    }

    @Test
    void deepSearch_returnsPageResponse() throws SearchQueryException {
        PageResponse<PublicationResponseDTO> pageResponse = new PageResponse<>();
        when(searchService.searchWithAdvancedFilter(
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(),
                anyInt(), anyInt(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any())
        ).thenReturn(pageResponse);

        PageResponse<PublicationResponseDTO> response = controller.deepSearch(
                UUID.randomUUID(), UUID.randomUUID(), "query", 0, 5,
                null, null, null, null, null,
                null, null, null, null, null, null, null
        );

        assertThat(response).isEqualTo(pageResponse);
        verify(searchService, times(1)).searchWithAdvancedFilter(
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(),
                anyInt(), anyInt(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    void inputPrediction_returnsPredictions() throws SearchQueryException {
        List<InputPredictionResponseDTO> predictions = List.of(new InputPredictionResponseDTO());
        when(inputPredictionService.getInputPredictions("query")).thenReturn(predictions);

        ResponseEntity<List<InputPredictionResponseDTO>> response = controller.inputPrediction("query");

        assertThat(response.getBody()).isEqualTo(predictions);
        verify(inputPredictionService, times(1)).getInputPredictions("query");
    }

    @Test
    void elasticIndexation_returnsString() {
        when(inputPredictionService.indexInputPredictionDocuments()).thenReturn("ok");

        ResponseEntity<String> response = controller.elasticIndexation();

        assertThat(response.getBody()).isEqualTo("ok");
        verify(inputPredictionService, times(1)).indexInputPredictionDocuments();
    }

    @Test
    void findAllLatestPublications_returnsPageResponse() {
        PageResponse<PublicationResponseDTO> pageResponse = new PageResponse<>();
        when(searchService.findAllLatestPublications(
                anyInt(), anyInt(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(pageResponse);

        ResponseEntity<PageResponse<PublicationResponseDTO>> response = controller.findAllLatestPublications(
                null, null, null, null, null, null, null, 0, 5, mock(Authentication.class)
        );

        assertThat(response.getBody()).isEqualTo(pageResponse);
        verify(searchService, times(1)).findAllLatestPublications(
                anyInt(), anyInt(), any(), any(), any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    void getFoundPublicationsCount_returnsDTO() throws SearchQueryException {
        FoundPublicationsDTO dto = new FoundPublicationsDTO();
        when(searchService.getPublicationsCount(
                any(), any(), any(), anyInt(), anyInt(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any()
        )).thenReturn(dto);

        ResponseEntity<FoundPublicationsDTO> response = controller.getFoundPublicationsCount(
                null, null, "query", null, null, null, null, null, null, null, null, null, null, 5, 10
        );

        assertThat(response.getBody()).isEqualTo(dto);
        verify(searchService, times(1)).getPublicationsCount(
                any(), any(), any(), anyInt(), anyInt(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    void getLastQueriedValues_returnsList() {
        List<String> lastQueries = List.of("query1", "query2");
        when(searchService.getLastQueriedValues(any())).thenReturn(lastQueries);

        ResponseEntity<List<String>> response = controller.getLastQueriedValues(mock(Authentication.class));

        assertThat(response.getBody()).isEqualTo(lastQueries);
        verify(searchService, times(1)).getLastQueriedValues(any());
    }
}
