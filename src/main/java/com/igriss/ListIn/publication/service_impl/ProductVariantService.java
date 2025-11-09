package com.igriss.ListIn.publication.service_impl;

import com.igriss.ListIn.config.Images.S3Service;
import com.igriss.ListIn.publication.dto.ProductVariantRequestDTO;
import com.igriss.ListIn.publication.dto.ProductVariantResponseDTO;
import com.igriss.ListIn.publication.entity.ProductVariant;
import com.igriss.ListIn.publication.entity.Publication;
import com.igriss.ListIn.publication.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final S3Service s3Service;

    public ProductVariant save(ProductVariantRequestDTO request, Publication publication) {

        ProductVariant productVariant = ProductVariant.builder()
                .size(request.getSize())
                .shoeSize(request.getShoeSize())
                .color(request.getColor())
                .images(request.getImageUrls())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .stock(request.getStock())
                .sku(request.getSku())
                .publication(publication)
                .build();
        return productVariantRepository.save(productVariant);
    }

    public ProductVariant update(UUID variantId, ProductVariantResponseDTO request, Publication publication) {
        ProductVariant existing = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new NoSuchElementException("ProductVariant not found with id: " + variantId));

        List<String> oldImages = existing.getImages() != null ? existing.getImages() : new ArrayList<>();
        List<String> newImages = request.getImageUrls() != null ? request.getImageUrls() : new ArrayList<>();

        // find images to delete (in old but not in new)
        List<String> toDelete = oldImages.stream()
                .filter(img -> !newImages.contains(img))
                .toList();

        // find images to add (in new but not in old)
        List<String> toAdd = newImages.stream()
                .filter(img -> !oldImages.contains(img))
                .toList();

        // 1. Delete old ones from S3
        if (!toDelete.isEmpty()) {
            s3Service.deleteFiles(toDelete);
        }

        // 3. Update product variant with the new list
        existing.setSize(request.getSize());
        existing.setShoeSize(request.getShoeSize());
        existing.setColor(request.getColor());
        existing.setImages(newImages); // final updated list
        existing.setPrice(request.getPrice());
        existing.setDiscountPrice(request.getDiscountPrice());
        existing.setStock(request.getStock());
        existing.setSku(request.getSku());
        existing.setPublication(publication);

        return productVariantRepository.save(existing);
    }

    public List<ProductVariant> findByPublicationId(UUID publicationId) {
        return productVariantRepository.findByPublication_Id(publicationId);
    }
}
