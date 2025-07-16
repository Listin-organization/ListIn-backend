package com.igriss.ListIn.publication.dto;

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

    private Float price;

    private Boolean bargain;

    private Double aspectRation;

    private String productCondition;

    private Map<Boolean,List<String>> imageUrls;

    private Map<Boolean,String> videoUrl;


    private List<UpdatePublicationRequestDTO.AttributeValueDTO> attributeValues;

    @Getter
    @Setter
    @Builder
    public static class AttributeValueDTO {
        private UUID attributeId;
        private List<UUID> attributeValueIds;
    }
}
