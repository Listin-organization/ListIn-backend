package com.igriss.ListIn.search.service;

import com.igriss.ListIn.exceptions.PublicationNotFoundException;
import com.igriss.ListIn.publication.dto.UpdatePublicationRequestDTO;
import com.igriss.ListIn.publication.entity.AttributeKey;
import com.igriss.ListIn.publication.entity.AttributeValue;
import com.igriss.ListIn.publication.entity.NumericField;
import com.igriss.ListIn.publication.entity.NumericValue;
import com.igriss.ListIn.publication.entity.Publication;
import com.igriss.ListIn.publication.entity.PublicationAttributeValue;
import com.igriss.ListIn.search.document.AttributeKeyDocument;
import com.igriss.ListIn.search.document.AttributeValueDocument;
import com.igriss.ListIn.search.document.PublicationDocument;
import com.igriss.ListIn.search.mapper.PublicationDocumentMapper;
import com.igriss.ListIn.search.repository.PublicationDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PublicationDocumentServiceImplTest {

    @Mock
    private PublicationDocumentMapper mapper;

    @Mock
    private PublicationDocumentRepository repository;

    @InjectMocks
    private PublicationDocumentServiceImpl service;

    private Publication publication;
    private PublicationAttributeValue pav;
    private AttributeValue attributeValue;
    private AttributeKey attributeKey;

    private NumericValue numericValue;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        publication = new Publication();
        publication.setId(UUID.randomUUID());

        attributeKey = new AttributeKey();
        attributeKey.setId(UUID.randomUUID());
        attributeKey.setName("Color");

        attributeValue = new AttributeValue();
        attributeValue.setId(UUID.randomUUID());
        attributeValue.setValue("Red");
        attributeValue.setAttributeKey(attributeKey);

        pav = new PublicationAttributeValue();
        pav.setAttributeValue(attributeValue);

        numericValue = new NumericValue();
        NumericField numericField = new NumericField();
        numericField.setId(UUID.randomUUID());
        numericValue.setNumericField(numericField);
    }

    @Test
    void saveIntoPublicationDocument_ShouldCallRepositorySave() {
        AttributeKeyDocument attributeKeyDocument = AttributeKeyDocument.builder()
                .id(attributeKey.getId())
                .key(attributeKey.getName())
                .attributeValues(List.of(AttributeValueDocument.builder()
                        .id(attributeValue.getId())
                        .value(attributeValue.getValue())
                        .build()))
                .build();

        PublicationDocument publicationDocument = new PublicationDocument();
        when(mapper.toPublicationDocument(eq(publication), anyList(), anyList())).thenReturn(publicationDocument);

        service.saveIntoPublicationDocument(publication, List.of(pav), List.of(numericValue));

        verify(repository).save(publicationDocument);
    }

    @Test
    void updateInPublicationDocument_ShouldUpdateFields() {
        UUID pubId = UUID.randomUUID();
        PublicationDocument existing = new PublicationDocument();
        existing.setTitle("Old Title");
        existing.setDescription("Old Desc");
        existing.setBargain(false);

        when(repository.findById(pubId)).thenReturn(Optional.of(existing));

        UpdatePublicationRequestDTO updateDTO = new UpdatePublicationRequestDTO();
        updateDTO.setTitle("New Title");
        updateDTO.setDescription("New Desc");
        updateDTO.setBargain(true);
        updateDTO.setProductCondition("USED_PRODUCT");

        service.updateInPublicationDocument(pubId, updateDTO);

        assertEquals("New Title", existing.getTitle());
        assertEquals("New Desc", existing.getDescription());
        assertTrue(existing.getBargain());

        verify(repository).save(existing);
    }

    @Test
    void updateInPublicationDocument_ShouldThrowException_WhenNotFound() {
        UUID pubId = UUID.randomUUID();
        when(repository.findById(pubId)).thenReturn(Optional.empty());

        UpdatePublicationRequestDTO updateDTO = new UpdatePublicationRequestDTO();

        assertThrows(PublicationNotFoundException.class, () -> service.updateInPublicationDocument(pubId, updateDTO));
    }

    @Test
    void deleteById_ShouldCallRepositoryDelete() {
        UUID pubId = UUID.randomUUID();
        service.deleteById(pubId);
        verify(repository).deleteById(pubId);
    }
}
