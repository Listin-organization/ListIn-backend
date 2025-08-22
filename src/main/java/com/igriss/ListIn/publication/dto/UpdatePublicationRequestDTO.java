package com.igriss.ListIn.publication.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Builder
@Getter
@Setter
public class UpdatePublicationRequestDTO {

    private String title;

    private String description;

    @DecimalMin(value = "10000", message = "Price must be at least 10,000")
    @DecimalMax(value = "1000000000", message = "Price must be at most 1,000,000,000")
    private Float price;

    private Boolean bargain;

    private Double aspectRation;

    private String videoPreview;

    private String productCondition;

    private Map<Boolean,List<String>> imageUrls;

    private Map<Boolean,String> videoUrl;

    private List<ProductVariantResponseDTO> productVariants;

    private List<UpdatePublicationRequestDTO.AttributeValueDTO> attributeValues;

    @Getter
    @Setter
    @Builder
    public static class AttributeValueDTO {
        private UUID attributeId;
        private List<UUID> attributeValueIds;
    }
}
