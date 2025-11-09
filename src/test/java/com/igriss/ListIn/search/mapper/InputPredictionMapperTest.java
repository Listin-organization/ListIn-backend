package com.igriss.ListIn.search.mapper;

import com.igriss.ListIn.publication.entity.AttributeKey;
import com.igriss.ListIn.publication.entity.AttributeValue;
import com.igriss.ListIn.publication.entity.CategoryAttribute;
import com.igriss.ListIn.publication.entity.static_entity.Category;
import com.igriss.ListIn.search.document.InputPredictionDocument;
import com.igriss.ListIn.search.dto.InputPredictionResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InputPredictionMapperTest {

    private InputPredictionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new InputPredictionMapper();
    }

    @Test
    void toInputPredictionResponseDTO_mapsAllFieldsCorrectly() {
        UUID id = UUID.randomUUID();
        UUID parentValueId = UUID.randomUUID();
        UUID parentKeyId = UUID.randomUUID();
        UUID childKeyId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID parentCategoryId = UUID.randomUUID();

        InputPredictionDocument document = InputPredictionDocument.builder()
                .id(id)
                .childAttributeValue("childValue")
                .parentAttributeValueId(parentValueId)
                .parentAttributeValue("parentValue")
                .parentAttributeKeyId(parentKeyId)
                .childAttributeKeyId(childKeyId)
                .categoryId(categoryId)
                .categoryName("categoryName")
                .parentCategoryId(parentCategoryId)
                .parentCategoryName("parentCategoryName")
                .build();

        InputPredictionResponseDTO dto = mapper.toInputPredictionResponseDTO(document);

        assertThat(dto.getChildAttributeValueId()).isEqualTo(id);
        assertThat(dto.getChildAttributeValue()).isEqualTo("childValue");
        assertThat(dto.getParentAttributeValueId()).isEqualTo(parentValueId);
        assertThat(dto.getParentAttributeValue()).isEqualTo("parentValue");
        assertThat(dto.getParentAttributeKeyId()).isEqualTo(parentKeyId);
        assertThat(dto.getChildAttributeKeyId()).isEqualTo(childKeyId);
        assertThat(dto.getCategoryId()).isEqualTo(categoryId);
        assertThat(dto.getCategoryName()).isEqualTo("categoryName");
        assertThat(dto.getParentCategoryId()).isEqualTo(parentCategoryId);
        assertThat(dto.getParentCategoryName()).isEqualTo("parentCategoryName");
    }

    @Test
    void toInputPredictionDocument_mapsAllFieldsCorrectly() {
        UUID parentValueId = UUID.randomUUID();
        UUID parentKeyId = UUID.randomUUID();
        UUID childKeyId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID parentCategoryId = UUID.randomUUID();

        AttributeKey attributeKey = AttributeKey.builder()
                .id(childKeyId)
                .build();

        Category parentCategory = Category.builder()
                .id(parentCategoryId)
                .name("parentCategoryName")
                .build();

        Category category = Category.builder()
                .id(categoryId)
                .name("categoryName")
                .parentCategory(parentCategory)
                .build();

        CategoryAttribute ca = CategoryAttribute.builder()
                .attributeKey(attributeKey)
                .category(category)
                .build();

        AttributeValue parentValue = AttributeValue.builder()
                .id(parentValueId)
                .value("parentValue")
                .attributeKey(AttributeKey.builder().id(parentKeyId).build())
                .build();

        AttributeValue av = AttributeValue.builder()
                .id(UUID.randomUUID())
                .value("childValue")
                .parentValue(parentValue)
                .attributeKey(attributeKey)
                .build();

        InputPredictionDocument document = mapper.toInputPredictionDocument(av, ca);

        assertThat(document.getId()).isEqualTo(av.getId());
        assertThat(document.getChildAttributeValue()).isEqualTo("childValue");
        assertThat(document.getParentAttributeValueId()).isEqualTo(parentValueId);
        assertThat(document.getParentAttributeValue()).isEqualTo("parentValue");
        assertThat(document.getParentAttributeKeyId()).isEqualTo(parentKeyId);
        assertThat(document.getChildAttributeKeyId()).isEqualTo(childKeyId);
        assertThat(document.getCategoryId()).isEqualTo(categoryId);
        assertThat(document.getCategoryName()).isEqualTo("categoryName");
        assertThat(document.getParentCategoryId()).isEqualTo(parentCategoryId);
        assertThat(document.getParentCategoryName()).isEqualTo("parentCategoryName");
    }

    @Test
    void toInputPredictionDocument_handlesNullParentValue() {
        UUID childKeyId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID parentCategoryId = UUID.randomUUID();

        AttributeKey attributeKey = AttributeKey.builder().id(childKeyId).build();

        Category parentCategory = Category.builder()
                .id(parentCategoryId)
                .name("parentCategoryName")
                .build();

        Category category = Category.builder()
                .id(categoryId)
                .name("categoryName")
                .parentCategory(parentCategory)
                .build();

        CategoryAttribute ca = CategoryAttribute.builder()
                .attributeKey(attributeKey)
                .category(category)
                .build();

        AttributeValue av = AttributeValue.builder()
                .id(UUID.randomUUID())
                .value("childValue")
                .parentValue(null)
                .attributeKey(attributeKey)
                .build();

        InputPredictionDocument document = mapper.toInputPredictionDocument(av, ca);

        assertThat(document.getParentAttributeValueId()).isNull();
        assertThat(document.getParentAttributeValue()).isNull();
        assertThat(document.getParentAttributeKeyId()).isNull();
    }
}
