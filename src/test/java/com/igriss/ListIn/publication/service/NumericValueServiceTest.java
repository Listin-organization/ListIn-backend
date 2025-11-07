package com.igriss.ListIn.publication.service;


import com.igriss.ListIn.exceptions.ResourceNotFoundException;
import com.igriss.ListIn.publication.dto.NumericValueRequestDTO;
import com.igriss.ListIn.publication.entity.NumericField;
import com.igriss.ListIn.publication.entity.NumericValue;
import com.igriss.ListIn.publication.entity.Publication;
import com.igriss.ListIn.publication.repository.NumericFieldRepository;
import com.igriss.ListIn.publication.repository.NumericValueRepository;
import com.igriss.ListIn.publication.service_impl.NumericValueServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NumericValueServiceTest {

    @Mock
    private NumericFieldRepository numericFieldRepository;

    @Mock
    private NumericValueRepository numericValueRepository;

    @InjectMocks
    private NumericValueServiceImpl numericValueService;

    private Publication publication;
    private NumericField numericField;
    private NumericValueRequestDTO numericValueRequestDTO;

    @BeforeEach
    void setUp() {
        publication = Publication.builder()
                .id(UUID.randomUUID())
                .title("Test Publication")
                .build();

        numericField = NumericField.builder()
                .id(UUID.randomUUID())
                .fieldName("Battery Capacity")
                .description("Measured in mAh")
                .build();

        numericValueRequestDTO = NumericValueRequestDTO.builder()
                .numericFieldId(numericField.getId())
                .numericValue(4500L)
                .build();
    }

    @Test
    void shouldSavePublicationNumericValuesSuccessfully() {
        when(numericFieldRepository.findById(numericField.getId())).thenReturn(Optional.of(numericField));

        NumericValue savedNumericValue = NumericValue.builder()
                .id(UUID.randomUUID())
                .numericField(numericField)
                .publication(publication)
                .value(4500L)
                .build();

        when(numericValueRepository.saveAll(anyList())).thenReturn(List.of(savedNumericValue));

        List<NumericValue> result = numericValueService.savePublicationNumericValues(List.of(numericValueRequestDTO), publication);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNumericField().getFieldName()).isEqualTo("Battery Capacity");
        assertThat(result.get(0).getPublication()).isEqualTo(publication);
        assertThat(result.get(0).getValue()).isEqualTo(4500);

        verify(numericFieldRepository, times(1)).findById(numericField.getId());
        verify(numericValueRepository, times(1)).saveAll(anyList());
    }

    @Test
    void shouldReturnNullWhenRequestIsEmpty() {
        List<NumericValue> result = numericValueService.savePublicationNumericValues(Collections.emptyList(), publication);

        assertThat(result).isNull();
        verifyNoInteractions(numericFieldRepository, numericValueRepository);
    }

    @Test
    void shouldReturnNullWhenRequestIsNull() {
        List<NumericValue> result = numericValueService.savePublicationNumericValues(null, publication);

        assertThat(result).isNull();
        verifyNoInteractions(numericFieldRepository, numericValueRepository);
    }

    @Test
    void shouldThrowExceptionWhenNumericFieldNotFound() {
        when(numericFieldRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                numericValueService.savePublicationNumericValues(List.of(numericValueRequestDTO), publication)
        ).isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("does not exist");

        verify(numericFieldRepository, times(1)).findById(any(UUID.class));
        verifyNoInteractions(numericValueRepository);
    }

    @Test
    void shouldDeletePublicationNumericFieldsByPublicationId() {
        UUID publicationId = UUID.randomUUID();

        numericValueService.deletePublicationNumericFields(publicationId);

        verify(numericValueRepository, times(1)).deleteAllByPublication_Id(publicationId);
    }

    @Test
    void shouldFindNumericFieldsByPublicationId() {
        UUID publicationId = UUID.randomUUID();
        NumericValue numericValue = NumericValue.builder()
                .id(UUID.randomUUID())
                .publication(publication)
                .numericField(numericField)
                .value(999L)
                .build();

        when(numericValueRepository.findAllByPublication_Id(publicationId)).thenReturn(List.of(numericValue));

        List<NumericValue> result = numericValueService.findNumericFields(publicationId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getValue()).isEqualTo(999);

        verify(numericValueRepository, times(1)).findAllByPublication_Id(publicationId);
    }
}
