package com.igriss.ListIn.publication.service;

import com.igriss.ListIn.exceptions.ResourceNotFoundException;
import com.igriss.ListIn.exceptions.ValidationException;
import com.igriss.ListIn.publication.dto.PublicationRequestDTO;
import com.igriss.ListIn.publication.entity.AttributeKey;
import com.igriss.ListIn.publication.entity.AttributeValue;
import com.igriss.ListIn.publication.entity.CategoryAttribute;
import com.igriss.ListIn.publication.entity.Publication;
import com.igriss.ListIn.publication.entity.PublicationAttributeValue;
import com.igriss.ListIn.publication.entity.static_entity.Category;
import com.igriss.ListIn.publication.repository.AttributeValueRepository;
import com.igriss.ListIn.publication.repository.CategoryAttributeRepository;
import com.igriss.ListIn.publication.repository.PublicationAttributeValueRepository;
import com.igriss.ListIn.publication.service_impl.PublicationAttributeValueServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class PublicationAttributeValueServiceTest {

    @Mock
    private CategoryAttributeRepository categoryAttributeRepository;

    @Mock
    private AttributeValueRepository attributeValueRepository;

    @Mock
    private PublicationAttributeValueRepository publicationAttributeValueRepository;

    @InjectMocks
    private PublicationAttributeValueServiceImpl service;

    private Publication publication;
    private Category category;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        category = new Category();
        category.setId(UUID.randomUUID());
        publication = Publication.builder().id(UUID.randomUUID()).category(category).build();
    }

    private PublicationRequestDTO.AttributeValueDTO createAttrDTO(UUID attrId, UUID valueId) {
        return PublicationRequestDTO.AttributeValueDTO.builder()
                .attributeId(attrId)
                .attributeValueIds(List.of(valueId))
                .build();
    }

    @Test
    void savePublicationAttributeValues_ShouldSaveAll_ForValidMultiSelectableAttribute() {
        UUID attributeId = UUID.randomUUID();
        UUID valueId = UUID.randomUUID();


        PublicationRequestDTO.AttributeValueDTO dto = createAttrDTO(attributeId, valueId);


        AttributeKey attributeKey = AttributeKey.builder().id(attributeId).widgetType("multiSelectable").build();
        CategoryAttribute categoryAttribute = CategoryAttribute.builder().attributeKey(attributeKey).build();

        when(categoryAttributeRepository.findByCategory_Id(category.getId()))
                .thenReturn(List.of(categoryAttribute));

        AttributeValue attributeValue = AttributeValue.builder().id(valueId).build();
        when(attributeValueRepository.findById(valueId)).thenReturn(Optional.of(attributeValue));

        PublicationAttributeValue saved = PublicationAttributeValue.builder().id(UUID.randomUUID()).build();
        when(publicationAttributeValueRepository.save(any(PublicationAttributeValue.class)))
                .thenReturn(saved);


        List<PublicationAttributeValue> result = service.savePublicationAttributeValues(List.of(dto), publication, List.of());


        assertThat(result).hasSize(1);
        verify(publicationAttributeValueRepository, times(1)).save(any(PublicationAttributeValue.class));
    }

    @Test
    void savePublicationAttributeValues_ShouldThrowValidationException_WhenInvalidAttributeForCategory() {
        UUID attrId = UUID.randomUUID();
        PublicationRequestDTO.AttributeValueDTO dto = createAttrDTO(attrId, UUID.randomUUID());


        when(categoryAttributeRepository.findByCategory_Id(category.getId()))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> service.savePublicationAttributeValues(List.of(dto), publication, List.of()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid attribute for category");

        verifyNoInteractions(attributeValueRepository);
    }

    @Test
    void savePublicationAttributeValues_ShouldThrowValidationException_WhenMultipleValuesForOneSelectable() {
        UUID attrId = UUID.randomUUID();
        PublicationRequestDTO.AttributeValueDTO dto = PublicationRequestDTO.AttributeValueDTO.builder()
                .attributeId(attrId)
                .attributeValueIds(List.of(UUID.randomUUID(), UUID.randomUUID()))
                .build();

        AttributeKey key = AttributeKey.builder().id(attrId).widgetType("oneSelectable").build();
        CategoryAttribute categoryAttribute = CategoryAttribute.builder().attributeKey(key).build();

        when(categoryAttributeRepository.findByCategory_Id(category.getId()))
                .thenReturn(List.of(categoryAttribute));

        assertThatThrownBy(() -> service.savePublicationAttributeValues(List.of(dto), publication, List.of()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("allows only one value");

        verifyNoInteractions(attributeValueRepository);
    }

    @Test
    void savePublicationAttributeValues_ShouldThrowResourceNotFound_WhenAttributeValueMissing() {
        UUID attrId = UUID.randomUUID();
        UUID missingValueId = UUID.randomUUID();

        PublicationRequestDTO.AttributeValueDTO dto = createAttrDTO(attrId, missingValueId);

        AttributeKey key = AttributeKey.builder().id(attrId).widgetType("multiSelectable").build();
        CategoryAttribute categoryAttribute = CategoryAttribute.builder().attributeKey(key).build();

        when(categoryAttributeRepository.findByCategory_Id(category.getId()))
                .thenReturn(List.of(categoryAttribute));

        when(attributeValueRepository.findById(missingValueId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.savePublicationAttributeValues(List.of(dto), publication, List.of()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Attribute value not found");

        verify(attributeValueRepository, times(1)).findById(missingValueId);
        verifyNoInteractions(publicationAttributeValueRepository);
    }

    @Test
    void deletePublicationAttributes_ShouldCallRepositoryDelete() {
        UUID publicationId = UUID.randomUUID();

        service.deletePublicationAttributes(publicationId);

        verify(publicationAttributeValueRepository, times(1))
                .deleteAllByPublication_Id(publicationId);
        verifyNoMoreInteractions(publicationAttributeValueRepository);
    }
}

