package com.igriss.ListIn.publication.mapper;

import com.igriss.ListIn.location.dto.LocationDTO;
import com.igriss.ListIn.location.entity.Country;
import com.igriss.ListIn.location.entity.County;
import com.igriss.ListIn.location.entity.State;
import com.igriss.ListIn.location.mapper.LocationMapper;
import com.igriss.ListIn.publication.dto.ProductVariantResponseDTO;
import com.igriss.ListIn.publication.dto.PublicationRequestDTO;
import com.igriss.ListIn.publication.dto.PublicationResponseDTO;
import com.igriss.ListIn.publication.entity.NumericValue;
import com.igriss.ListIn.publication.entity.ProductVariant;
import com.igriss.ListIn.publication.entity.Publication;
import com.igriss.ListIn.publication.entity.PublicationImage;
import com.igriss.ListIn.publication.entity.static_entity.Category;
import com.igriss.ListIn.publication.enums.PublicationType;
import com.igriss.ListIn.security.roles.Role;
import com.igriss.ListIn.user.entity.User;
import com.igriss.ListIn.user.mapper.UserMapper;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

class PublicationMapperTest {

    @Mock
    private PublicationAttributeValueMapper publicationAttributeValueMapper;

    @Mock
    private PublicationImageMapper publicationImageMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private LocationMapper locationMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private PublicationMapper publicationMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void test_ProductVariantMapper_toResponse() {

        var variant = Instancio.of(ProductVariant.class)
                .set(Select.field("variantId"), UUID.randomUUID())
                .create();

        ProductVariantResponseDTO dto = ProductVariantMapper.toResponse(variant);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(variant.getVariantId());
        assertThat(dto.getColor()).isEqualTo(variant.getColor());
        assertThat(dto.getPrice()).isEqualTo(variant.getPrice());
        assertThat(dto.getImageUrls()).isEqualTo(variant.getImages());
    }

    @Test
    void test_PublicationMapper_toPublication() {

        var req = Instancio.of(PublicationRequestDTO.class)
                .set(Select.field("productCondition"), "NEW_PRODUCT")
                .set(Select.field("aspectRation"), null)
                .create();

        User user = Instancio.of(User.class)
                .set(Select.field("role"), Role.BUSINESS_SELLER)
                .create();

        LocationDTO location = new LocationDTO(
                Instancio.create(Country.class),
                Instancio.create(State.class),
                Instancio.create(County.class)
        );

        when(categoryMapper.toCategory(req.getCategoryId()))
                .thenReturn(Instancio.create(Category.class));

        Publication publication = publicationMapper.toPublication(req, user, location);

        assertThat(publication).isNotNull();
        assertThat(publication.getSeller()).isEqualTo(user);
        assertThat(publication.getPublicationType()).isEqualTo(PublicationType.BUSINESS_PUBLICATION);
        assertThat(publication.getProductCondition().name()).isEqualTo(req.getProductCondition());
        assertThat(publication.getAspectRation()).isEqualTo(1.0);
        assertThat(publication.getPrice()).isEqualTo(req.getPrice());
    }

    @Test
    void test_PublicationMapper_toPublicationResponseDTO() {

        Publication publication = Instancio.of(Publication.class)
                .set(Select.field(Publication::getPrice), 0F) // to test isFree
                .create();

        List<PublicationImage> images = Instancio.ofList(PublicationImage.class).size(2).create();
        List<NumericValue> numericValues = Instancio.ofList(NumericValue.class).size(2).create();

        when(publicationImageMapper.toImageDTOList(images))
                .thenReturn(List.of(Instancio.create(com.igriss.ListIn.publication.dto.ImageDTO.class)));

        when(categoryMapper.toCategoryResponseDTO(publication.getCategory()))
                .thenReturn(Instancio.create(com.igriss.ListIn.publication.dto.CategoryDTO.class));

        when(userMapper.toUserResponseDTO(eq(publication.getSeller()), anyBoolean()))
                .thenReturn(Instancio.create(com.igriss.ListIn.user.dto.UserResponseDTO.class));

        when(locationMapper.toCountryDTO(publication.getCountry()))
                .thenReturn(Instancio.create(com.igriss.ListIn.location.dto.CountryDTO.class));

        when(locationMapper.toStateDTO(publication.getState()))
                .thenReturn(Instancio.create(com.igriss.ListIn.location.dto.StateDTO.class));

        when(locationMapper.toCountyDTO(publication.getCounty()))
                .thenReturn(Instancio.create(com.igriss.ListIn.location.dto.CountyDTO.class));

        when(publicationAttributeValueMapper.toPublicationAttributeValueDTO(publication, numericValues))
                .thenReturn(Instancio.create(com.igriss.ListIn.publication.dto.PublicationAttributeValueDTO.class));

        List<ProductVariantResponseDTO> variants = Instancio.ofList(ProductVariantResponseDTO.class).size(2).create();

        PublicationResponseDTO dto = publicationMapper.toPublicationResponseDTO(
                publication,
                images,
                "video.mp4",
                numericValues,
                true,
                true,
                variants
        );

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(publication.getId());
        assertThat(dto.getIsLiked()).isTrue();
        assertThat(dto.getVideoUrl()).isEqualTo("video.mp4");
        assertThat(dto.getIsFree()).isTrue();
        assertThat(dto.getProductVariants()).hasSize(2);
        assertThat(dto.getCategory()).isNotNull();
        assertThat(dto.getSeller()).isNotNull();
    }

    @Test
    void test_PublicationMapper_toPublication_AdminPublicationType() {

        PublicationRequestDTO req = Instancio.of(PublicationRequestDTO.class)
                .set(Select.field("productCondition"), "USED_PRODUCT")
                .create();

        User adminUser = Instancio.of(User.class)
                .set(Select.field("role"), Role.ADMIN)
                .create();

        LocationDTO location = new LocationDTO(
                Instancio.create(Country.class),
                Instancio.create(State.class),
                Instancio.create(County.class)
        );

        when(categoryMapper.toCategory(req.getCategoryId()))
                .thenReturn(Instancio.create(Category.class));

        Publication publication = publicationMapper.toPublication(req, adminUser, location);

        assertThat(publication).isNotNull();
        assertThat(publication.getPublicationType())
                .isEqualTo(PublicationType.ADVERTISEMENT_PUBLICATION);

        assertThat(publication.getProductCondition().name())
                .isEqualTo(req.getProductCondition());
    }

}
