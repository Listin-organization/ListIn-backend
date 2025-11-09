package com.igriss.ListIn.search.mapper;

import com.igriss.ListIn.location.entity.Country;
import com.igriss.ListIn.location.entity.County;
import com.igriss.ListIn.location.entity.State;
import com.igriss.ListIn.publication.entity.Publication;
import com.igriss.ListIn.publication.entity.static_entity.Category;
import com.igriss.ListIn.publication.enums.ProductCondition;
import com.igriss.ListIn.search.document.AttributeKeyDocument;
import com.igriss.ListIn.search.document.PublicationDocument;
import com.igriss.ListIn.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PublicationDocumentMapperTest {

    private PublicationDocumentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PublicationDocumentMapper();
    }

    @Test
    void toPublicationDocument_mapsAllFieldsCorrectly() {
        UUID publicationId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID parentCategoryId = UUID.randomUUID();
        UUID countryId = UUID.randomUUID();
        UUID stateId = UUID.randomUUID();
        UUID countyId = UUID.randomUUID();

        Category parentCategory = Category.builder()
                .id(parentCategoryId)
                .description("parentCategoryDescription")
                .build();

        Category category = Category.builder()
                .id(categoryId)
                .description("categoryDescription")
                .parentCategory(parentCategory)
                .build();

        User seller = User.builder()
                .role(null)
                .build();

        Country country = Country.builder().id(countryId).build();
        State state = State.builder().id(stateId).build();
        County county = County.builder().id(countyId).build();

        Publication publication = Publication.builder()
                .id(publicationId)
                .title("Test Title")
                .description("Test Description")
                .locationName("Test Location")
                .price(100.0F)
                .bargain(true)
                .productCondition(ProductCondition.NEW_PRODUCT)
                .category(category)
                .seller(seller)
                .country(country)
                .state(state)
                .county(county)
                .build();

        AttributeKeyDocument attributeKeyDocument = AttributeKeyDocument.builder()
                .id(UUID.randomUUID())
                .key("attributeKey")
                .build();

        PublicationDocument.NumericFieldDocument numericFieldDocument = PublicationDocument.NumericFieldDocument.builder()
                .fieldId(UUID.randomUUID())
                .value(10L)
                .build();

        PublicationDocument document = mapper.toPublicationDocument(
                publication,
                List.of(attributeKeyDocument),
                List.of(numericFieldDocument)
        );

        assertThat(document.getId()).isEqualTo(publicationId);
        assertThat(document.getTitle()).isEqualTo("Test Title");
        assertThat(document.getDescription()).isEqualTo("Test Description");
        assertThat(document.getLocationName()).isEqualTo("Test Location");
        assertThat(document.getPrice()).isEqualTo(100.0F);
        assertThat(document.getBargain()).isTrue();
        assertThat(document.getProductCondition()).isEqualTo(ProductCondition.NEW_PRODUCT);
        assertThat(document.getCategoryId()).isEqualTo(categoryId);
        assertThat(document.getCategoryDescription()).isEqualTo("categoryDescription");
        assertThat(document.getParentCategoryId()).isEqualTo(parentCategoryId);
        assertThat(document.getParentCategoryDescription()).isEqualTo("parentCategoryDescription");
        assertThat(document.getCountryId()).isEqualTo(countryId);
        assertThat(document.getStateId()).isEqualTo(stateId);
        assertThat(document.getCountyId()).isEqualTo(countyId);
        assertThat(document.getAttributeKeys()).contains(attributeKeyDocument);
        assertThat(document.getNumericFields()).contains(numericFieldDocument);
    }

    @Test
    void safeGet_returnsNullWhenNullValues() {
        Publication publication = Publication.builder()
                .id(UUID.randomUUID())
                .title("Test Title")
                .description("Test Description")
                .bargain(false)
                .productCondition(ProductCondition.NEW_PRODUCT)
                .build();

        PublicationDocument document = mapper.toPublicationDocument(publication, List.of(), List.of());

        assertThat(document.getLocationName()).isNull();
        assertThat(document.getCountryId()).isNull();
        assertThat(document.getStateId()).isNull();
        assertThat(document.getCountyId()).isNull();
        assertThat(document.getCategoryId()).isNull();
        assertThat(document.getCategoryDescription()).isNull();
        assertThat(document.getParentCategoryId()).isNull();
        assertThat(document.getParentCategoryDescription()).isNull();
        assertThat(document.getSellerType()).isNull();
    }
}
