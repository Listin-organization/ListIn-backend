package com.igriss.ListIn.publication.mapper;

import com.igriss.ListIn.publication.dto.ProductVariantResponseDTO;
import com.igriss.ListIn.publication.entity.ProductVariant;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProductVariantMapperTest {

    @Test
    void toResponse_shouldMapAllFieldsCorrectly() {
        UUID variantId = UUID.randomUUID();

        ProductVariant variant = ProductVariant.builder()
                .variantId(variantId)
                .size("L")
                .shoeSize("42")
                .color("Black")
                .images(List.of("img1.jpg", "img2.jpg"))
                .price(150.0)
                .discountPrice(120.0)
                .stock(5)
                .sku("SKU-12345")
                .build();

        ProductVariantResponseDTO dto = ProductVariantMapper.toResponse(variant);

        assertNotNull(dto);
        assertEquals(variantId, dto.getId());
        assertEquals("L", dto.getSize());
        assertEquals("42", dto.getShoeSize());
        assertEquals("Black", dto.getColor());
        assertEquals(List.of("img1.jpg", "img2.jpg"), dto.getImageUrls());
        assertEquals(150.0, dto.getPrice());
        assertEquals(120.0, dto.getDiscountPrice());
        assertEquals(5, dto.getStock());
        assertEquals("SKU-12345", dto.getSku());
    }

    @Test
    void toResponse_shouldThrowNullPointer_ifProductVariantNull() {
        assertThrows(NullPointerException.class,
                () -> ProductVariantMapper.toResponse(null));
    }

    @Test
    void toResponse_shouldHandleNullOptionalFields() {
        ProductVariant variant = ProductVariant.builder()
                .variantId(UUID.randomUUID())
                .size(null)
                .shoeSize(null)
                .color("Blue")
                .images(null)
                .price(50.0)
                .discountPrice(null)
                .stock(0)
                .sku("SKU-0001")
                .build();

        ProductVariantResponseDTO dto = ProductVariantMapper.toResponse(variant);

        assertNotNull(dto);
        assertNull(dto.getSize());
        assertNull(dto.getShoeSize());
        assertEquals("Blue", dto.getColor());
        assertNull(dto.getImageUrls());
        assertEquals(50.0, dto.getPrice());
        assertNull(dto.getDiscountPrice());
        assertEquals(0, dto.getStock());
        assertEquals("SKU-0001", dto.getSku());
    }
}
