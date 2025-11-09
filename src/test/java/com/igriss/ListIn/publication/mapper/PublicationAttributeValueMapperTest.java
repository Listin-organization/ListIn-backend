package com.igriss.ListIn.publication.mapper;

import com.igriss.ListIn.publication.dto.NumericValueResponseDTO;
import com.igriss.ListIn.publication.dto.PublicationAttributeValueDTO;
import com.igriss.ListIn.publication.entity.AttributeKey;
import com.igriss.ListIn.publication.entity.AttributeValue;
import com.igriss.ListIn.publication.entity.CategoryAttribute;
import com.igriss.ListIn.publication.entity.NumericField;
import com.igriss.ListIn.publication.entity.NumericValue;
import com.igriss.ListIn.publication.entity.Publication;
import com.igriss.ListIn.publication.entity.PublicationAttributeValue;
import com.igriss.ListIn.publication.entity.static_entity.Category;
import com.igriss.ListIn.publication.repository.PublicationAttributeValueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PublicationAttributeValueMapperTest {

    @Mock
    private PublicationAttributeValueRepository publicationAttributeValueRepository;

    @InjectMocks
    private PublicationAttributeValueMapper mapper;

    private Publication publication;
    private UUID pubId;
    private Category parentCat;
    private Category category;

    @BeforeEach
    void setup() {
        pubId = UUID.randomUUID();

        parentCat = new Category();
        parentCat.setId(UUID.randomUUID());
        parentCat.setName("Electronics");

        category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Smartphones");
        category.setParentCategory(parentCat);

        publication = new Publication();
        publication.setId(pubId);
        publication.setCategory(category);
    }

    @Test
    void toPublicationAttributeValueDTO_shouldMapCorrectly() {

        AttributeKey key = AttributeKey.builder()
                .filterText("Color")
                .filterTextUz("Rang")
                .filterTextRu("Цвет")
                .build();

        CategoryAttribute categoryAttribute = CategoryAttribute.builder()
                .attributeKey(key)
                .build();

        AttributeValue attrValue = AttributeValue.builder()
                .value("Black")
                .valueUz("Qora")
                .valueRu("Чёрный")
                .build();

        PublicationAttributeValue pav = PublicationAttributeValue.builder()
                .id(UUID.randomUUID())
                .publication(publication)
                .categoryAttribute(categoryAttribute)
                .attributeValue(attrValue)
                .build();

        when(publicationAttributeValueRepository.findByPublication_Id(pubId))
                .thenReturn(List.of(pav));

        NumericField numericField = NumericField.builder()
                .fieldName("Weight")
                .fieldNameUz("Vazn")
                .fieldNameRu("Вес")
                .build();

        NumericValue numericValue = NumericValue.builder()
                .id(UUID.randomUUID())
                .publication(publication)
                .numericField(numericField)
                .value(200L)
                .build();

        PublicationAttributeValueDTO dto = mapper.toPublicationAttributeValueDTO(publication, List.of(numericValue));

        assertEquals("Electronics", dto.getParentCategory());
        assertEquals("Smartphones", dto.getCategory());

        assertNotNull(dto.getAttributes());

        assertEquals(List.of("Black"), dto.getAttributes().get("en").get("Color"));
        assertEquals(List.of("Qora"), dto.getAttributes().get("uz").get("Rang"));
        assertEquals(List.of("Чёрный"), dto.getAttributes().get("ru").get("Цвет"));

        assertNotNull(dto.getNumericValues());
        NumericValueResponseDTO numericDto = dto.getNumericValues().get(0);

        assertEquals("Weight", numericDto.getNumericField());
        assertEquals("Vazn", numericDto.getNumericFieldUz());
        assertEquals("Вес", numericDto.getNumericFieldRu());
        assertEquals(200L, numericDto.getNumericValue());

        verify(publicationAttributeValueRepository).findByPublication_Id(pubId);
    }

    @Test
    void toPublicationAttributeValueDTO_shouldReturnNullNumericValues_whenListEmpty() {
        when(publicationAttributeValueRepository.findByPublication_Id(pubId))
                .thenReturn(Collections.emptyList());

        PublicationAttributeValueDTO dto = mapper.toPublicationAttributeValueDTO(publication, Collections.emptyList());

        assertNull(dto.getNumericValues());
        assertNotNull(dto.getAttributes());
    }

    @Test
    void toPublicationAttributeValueDTO_shouldReturnNullNumericValues_whenListNull() {
        when(publicationAttributeValueRepository.findByPublication_Id(pubId))
                .thenReturn(Collections.emptyList());

        PublicationAttributeValueDTO dto = mapper.toPublicationAttributeValueDTO(publication, null);

        assertNull(dto.getNumericValues());
        assertNotNull(dto.getAttributes());
    }
}
