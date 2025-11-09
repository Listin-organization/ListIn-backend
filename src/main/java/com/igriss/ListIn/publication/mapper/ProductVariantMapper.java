package com.igriss.ListIn.publication.mapper;

import com.igriss.ListIn.publication.dto.ProductVariantResponseDTO;
import com.igriss.ListIn.publication.entity.ProductVariant;

public class ProductVariantMapper {

    public static ProductVariantResponseDTO toResponse(ProductVariant productVariant){
        return ProductVariantResponseDTO.builder()
                .id(productVariant.getVariantId())
                .size(productVariant.getSize())
                .shoeSize(productVariant.getShoeSize())
                .color(productVariant.getColor())
                .imageUrls(productVariant.getImages())
                .price(productVariant.getPrice())
                .discountPrice(productVariant.getDiscountPrice())
                .stock(productVariant.getStock())
                .sku(productVariant.getSku())
                .build();
    }
}
