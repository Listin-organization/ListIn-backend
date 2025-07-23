package com.igriss.ListIn.publication.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
public class PublicationRequestDTO implements Serializable {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @DecimalMin(value = "10000", message = "Price must be at least 10,000")
    @DecimalMax(value = "1000000000", message = "Price must be at most 1,000,000,000")
    private Float price;

    private Boolean bargain;

    @Pattern(regexp = "^\\+998(\\s?\\d){9}$", message = "Phone Number must be in the format +998 00 123 45 67")
    private String phoneNumber;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime fromTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime toTime;

    private Boolean isGrantedForPreciseLocation;

    private String locationName;

    private String countryName;

    private String stateName;

    private String countyName;

    private Double latitude;

    private Double longitude;

    private String productCondition;

    private Double aspectRation;

    private String videoPreview;

    @NotEmpty(message = "At least an image is required")
    private List<String> imageUrls;

    private String videoUrl;

    private UUID categoryId;

    private List<NumericValueRequestDTO> numericValues;

    private List<AttributeValueDTO> attributeValues;

    @Getter
    @Setter
    @Builder
    public static class AttributeValueDTO {
        private UUID attributeId;
        private List<UUID> attributeValueIds;
    }
}
