package com.igriss.ListIn.search.service;

import com.igriss.ListIn.publication.entity.AttributeValue;
import com.igriss.ListIn.publication.entity.CategoryAttribute;
import com.igriss.ListIn.publication.repository.AttributeValueRepository;
import com.igriss.ListIn.publication.repository.CategoryAttributeRepository;
import com.igriss.ListIn.search.document.InputPredictionDocument;
import com.igriss.ListIn.search.dto.InputPredictionRequestDTO;
import com.igriss.ListIn.search.dto.InputPredictionResponseDTO;
import com.igriss.ListIn.search.mapper.InputPredictionMapper;
import com.igriss.ListIn.search.repository.InputPredictionDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InputPredictionServiceImplTest {

    @Mock
    private InputPredictionDocumentRepository predictionDocumentRepository;

    @Mock
    private InputPredictionMapper inputPredictionMapper;

    @Mock
    private AttributeValueRepository attributeValueRepository;

    @Mock
    private CategoryAttributeRepository categoryAttributeRepository;

    @InjectMocks
    private InputPredictionServiceImpl service;

    private InputPredictionDocument document;
    private InputPredictionResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        document = InputPredictionDocument.builder()
                .categoryId(UUID.randomUUID())
                .parentCategoryId(UUID.randomUUID())
                .parentCategoryName("Laptop Brand Model")
                .childAttributeValue("Dell XPS")
                .build();

        responseDTO = InputPredictionResponseDTO.builder()
                .categoryId(document.getCategoryId())
                .parentCategoryName(document.getParentCategoryName())
                .childAttributeValue(document.getChildAttributeValue())
                .build();
    }

    @Test
    void getInputPredictions_ShouldReturnMappedDTOs() {
        String model = "Laptop";
        PageRequest pageRequest = PageRequest.of(0, 5);

        when(predictionDocumentRepository.findByModelValueContainingIgnoreCase(model, pageRequest))
                .thenReturn(List.of(document));
        when(inputPredictionMapper.toInputPredictionResponseDTO(document)).thenReturn(responseDTO);

        List<InputPredictionResponseDTO> result = service.getInputPredictions(model);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(responseDTO.getChildAttributeValue(), result.get(0).getChildAttributeValue());
        verify(predictionDocumentRepository).findByModelValueContainingIgnoreCase(model, pageRequest);
        verify(inputPredictionMapper).toInputPredictionResponseDTO(document);
    }

    @Test
    void saveInputMatchDocument_ShouldCallRepositorySaveAll() {
        InputPredictionRequestDTO requestDTO = InputPredictionRequestDTO.builder()
                .categoryId(UUID.randomUUID())
                .parentCategoryId(UUID.randomUUID())
                .model("Dell XPS")
                .build();

        service.saveInputMatchDocument(List.of(requestDTO));

        ArgumentCaptor<List<InputPredictionDocument>> captor = ArgumentCaptor.forClass(List.class);
        verify(predictionDocumentRepository).saveAll(captor.capture());

        List<InputPredictionDocument> savedDocs = captor.getValue();
        assertEquals(1, savedDocs.size());
        assertEquals("Dell XPS", savedDocs.get(0).getChildAttributeValue());
    }
}
