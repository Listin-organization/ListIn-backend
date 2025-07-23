package com.igriss.ListIn.publication.dto;

import com.igriss.ListIn.location.dto.CountryDTO;
import com.igriss.ListIn.location.dto.CountyDTO;
import com.igriss.ListIn.location.dto.StateDTO;
import com.igriss.ListIn.publication.enums.ProductCondition;
import com.igriss.ListIn.publication.enums.PublicationType;
import com.igriss.ListIn.user.dto.UserResponseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
//todo -> add a jakarta validation to each field
public class PublicationResponseDTO implements Serializable {

    private UUID id;

    private String title;

    private String description;

    private Float price;

    private Boolean bargain;

    private Boolean isLiked;

    private Boolean isViewed;

    private Boolean isFree;

    private String sellerType;

    private List<ImageDTO> productImages;

    private String videoUrl;

    private PublicationType publicationType;

    private ProductCondition productCondition;

    private Long likes;

    private Long views;

    private Double aspectRation;

    private String videoPreview;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private CategoryDTO category;

    private UserResponseDTO seller;

    private Boolean isGrantedForPreciseLocation;

    private String locationName;

    private CountryDTO countryDTO;

    private StateDTO stateDTO;

    private CountyDTO countyDTO;

    private Double latitude;

    private Double longitude;

    private PublicationAttributeValueDTO attributeValue;

}
