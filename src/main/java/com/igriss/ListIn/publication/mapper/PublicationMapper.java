package com.igriss.ListIn.publication.mapper;


import com.igriss.ListIn.location.dto.LocationDTO;
import com.igriss.ListIn.location.mapper.LocationMapper;
import com.igriss.ListIn.publication.dto.PublicationRequestDTO;
import com.igriss.ListIn.publication.dto.PublicationResponseDTO;
import com.igriss.ListIn.publication.entity.NumericValue;
import com.igriss.ListIn.publication.entity.Publication;
import com.igriss.ListIn.publication.entity.PublicationImage;
import com.igriss.ListIn.publication.enums.ProductCondition;
import com.igriss.ListIn.publication.enums.PublicationStatus;
import com.igriss.ListIn.publication.enums.PublicationType;
import com.igriss.ListIn.user.entity.User;
import com.igriss.ListIn.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

//todo 1 -> write an implementation for advanced mapping from PublicationRequestDTO <-> Publication
//todo 2 -> also for PublicationResponseDTO <- Publication (The entire mapper part will be done by Davron)
@Slf4j
@Service
@RequiredArgsConstructor
public class PublicationMapper {

    private final PublicationAttributeValueMapper publicationAttributeValueMapper;
    private final PublicationImageMapper publicationImageMapper;
    private final CategoryMapper categoryMapper;
    private final LocationMapper locationMapper;
    private final UserMapper userMapper;


    public Publication toPublication(PublicationRequestDTO requestDTO, User connectedUser, LocationDTO location) {

        PublicationType publicationType = switch (connectedUser.getRole()) {
            case BUSINESS_SELLER -> PublicationType.BUSINESS_PUBLICATION;
            case ADMIN -> PublicationType.ADVERTISEMENT_PUBLICATION;
            default -> PublicationType.INDIVIDUAL_PUBLICATION;
        };

        ProductCondition productCondition = ProductCondition.valueOf(requestDTO.getProductCondition());

        PublicationStatus publicationStatus = PublicationStatus.PENDING_APPROVAL;

        return Publication.builder()
                .title(requestDTO.getTitle())
                .description(requestDTO.getDescription())
                .price(requestDTO.getPrice())
                .bargain(requestDTO.getBargain())
                .category(categoryMapper.toCategory(requestDTO.getCategoryId()))
                .productCondition(productCondition)
                .likes(0L)
                .views(0L)
                .aspectRation((requestDTO.getAspectRation() == null) ? 1.0 : requestDTO.getAspectRation())
                .videoPreview(requestDTO.getVideoPreview())
                .publicationType(publicationType)
                .publicationStatus(publicationStatus)
                .seller(connectedUser)
                .isGrantedForPreciseLocation(requestDTO.getIsGrantedForPreciseLocation())
                .locationName(requestDTO.getLocationName())
                .country(location.getCountry())
                .state(location.getState())
                .county(location.getCounty())
                .longitude(requestDTO.getLongitude())
                .latitude(requestDTO.getLatitude())
                .build();
    }

    public PublicationResponseDTO toPublicationResponseDTO(Publication publication, List<PublicationImage> publicationImages, String publicationVideo, List<NumericValue> numericValues, Boolean liked, Boolean following) {
        return PublicationResponseDTO.builder()
                .id(publication.getId())
                .title(publication.getTitle())
                .description(publication.getDescription())
                .price(publication.getPrice() != null ? publication.getPrice() : 0F)
                .bargain(publication.getBargain())
                .isLiked(liked)
                .aspectRation(publication.getAspectRation())
                .videoPreview(publication.getVideoPreview())
                .sellerType(publication.getSeller().getRole().name())
                .isFree(publication.getPrice() == null || publication.getPrice() == 0F)
                .productImages(publicationImageMapper.toImageDTOList(publicationImages))
                .videoUrl(publicationVideo)
                .publicationType(publication.getPublicationType())
                .productCondition(publication.getProductCondition())
                .likes(publication.getLikes() != null ? publication.getLikes() : 0L)
                .createdAt(publication.getDatePosted())
                .updatedAt(publication.getDateUpdated())
                .category(categoryMapper.toCategoryResponseDTO(publication.getCategory()))
                .seller(userMapper.toUserResponseDTO(publication.getSeller(), following))
                .isGrantedForPreciseLocation(publication.getIsGrantedForPreciseLocation())
                .locationName(publication.getLocationName())
                .countryDTO(locationMapper.toCountryDTO(publication.getCountry()))
                .stateDTO(locationMapper.toStateDTO(publication.getState()))
                .countyDTO(publication.getCounty()!= null ? locationMapper.toCountyDTO(publication.getCounty()) : null)
                .longitude(publication.getLongitude())
                .latitude(publication.getLatitude())
                .attributeValue(publicationAttributeValueMapper.toPublicationAttributeValueDTO(publication, numericValues))
                .build();
    }
}

