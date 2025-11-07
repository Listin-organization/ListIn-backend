package com.igriss.ListIn.publication.service;

import com.igriss.ListIn.config.Images.S3Service;
import com.igriss.ListIn.publication.dto.ProductVariantRequestDTO;
import com.igriss.ListIn.publication.dto.ProductVariantResponseDTO;
import com.igriss.ListIn.publication.entity.ProductVariant;
import com.igriss.ListIn.publication.entity.Publication;
import com.igriss.ListIn.publication.repository.ProductVariantRepository;
import com.igriss.ListIn.publication.service_impl.ProductVariantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductVariantServiceTest {

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private ProductVariantService productVariantService;

    private Publication publication;
    private UUID variantId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        publication = Publication.builder().id(UUID.randomUUID()).build();
        variantId = UUID.randomUUID();
    }

    @Test
    void save_shouldCreateAndSaveProductVariant() {
        ProductVariantRequestDTO request = ProductVariantRequestDTO.builder()
                .size("L")
                .shoeSize("42")
                .color("Black")
                .imageUrls(List.of("img1.jpg", "img2.jpg"))
                .price(100.0)
                .discountPrice(90.0)
                .stock(10)
                .sku("SKU123")
                .build();

        ProductVariant savedVariant = ProductVariant.builder()
                .variantId(variantId)
                .color("Black")
                .price(100.0)
                .build();

        when(productVariantRepository.save(any(ProductVariant.class))).thenReturn(savedVariant);

        ProductVariant result = productVariantService.save(request, publication);

        assertNotNull(result);
        assertEquals("Black", result.getColor());
        verify(productVariantRepository, times(1)).save(any(ProductVariant.class));
    }

    @Test
    void update_shouldUpdateExistingVariantAndDeleteOldImages() {
        ProductVariant existing = ProductVariant.builder()
                .variantId(variantId)
                .color("Red")
                .images(new ArrayList<>(List.of("old1.jpg", "old2.jpg")))
                .price(80.0)
                .sku("SKU123")
                .build();

        ProductVariantResponseDTO request = ProductVariantResponseDTO.builder()
                .color("Blue")
                .imageUrls(List.of("old2.jpg", "new1.jpg"))
                .price(120.0)
                .discountPrice(100.0)
                .stock(20)
                .sku("SKU123")
                .build();

        when(productVariantRepository.findById(variantId)).thenReturn(Optional.of(existing));
        when(productVariantRepository.save(any(ProductVariant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductVariant updated = productVariantService.update(variantId, request, publication);

        assertEquals("Blue", updated.getColor());
        assertEquals(List.of("old2.jpg", "new1.jpg"), updated.getImages());
        verify(s3Service, times(1)).deleteFiles(List.of("old1.jpg"));
        verify(productVariantRepository, times(1)).save(any(ProductVariant.class));
    }

    @Test
    void update_shouldThrowExceptionWhenVariantNotFound() {
        when(productVariantRepository.findById(variantId)).thenReturn(Optional.empty());

        ProductVariantResponseDTO request = ProductVariantResponseDTO.builder().build();

        assertThrows(NoSuchElementException.class,
                () -> productVariantService.update(variantId, request, publication));

        verify(productVariantRepository, never()).save(any());
    }

    @Test
    void update_shouldNotDeleteImagesIfNoDifference() {
        ProductVariant existing = ProductVariant.builder()
                .variantId(variantId)
                .images(List.of("same.jpg"))
                .build();

        ProductVariantResponseDTO request = ProductVariantResponseDTO.builder()
                .imageUrls(List.of("same.jpg"))
                .build();

        when(productVariantRepository.findById(variantId)).thenReturn(Optional.of(existing));
        when(productVariantRepository.save(any(ProductVariant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        productVariantService.update(variantId, request, publication);

        verify(s3Service, never()).deleteFiles(anyList());
        verify(productVariantRepository, times(1)).save(any(ProductVariant.class));
    }

    @Test
    void findByPublicationId_shouldReturnVariants() {
        List<ProductVariant> variants = List.of(
                ProductVariant.builder().variantId(UUID.randomUUID()).color("Black").build(),
                ProductVariant.builder().variantId(UUID.randomUUID()).color("White").build()
        );

        when(productVariantRepository.findByPublication_Id(publication.getId())).thenReturn(variants);

        List<ProductVariant> result = productVariantService.findByPublicationId(publication.getId());

        assertEquals(2, result.size());
        verify(productVariantRepository, times(1)).findByPublication_Id(publication.getId());
    }
}
