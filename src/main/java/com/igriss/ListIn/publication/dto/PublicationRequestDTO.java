package com.igriss.ListIn.publication.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    private String title;

    private String description;

    private Float price;

    private Boolean bargain;

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
