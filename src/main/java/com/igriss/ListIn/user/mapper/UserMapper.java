package com.igriss.ListIn.user.mapper;

import com.igriss.ListIn.location.mapper.LocationMapper;

import com.igriss.ListIn.user.dto.UserResponseDTO;
import com.igriss.ListIn.user.dto.WSUserResponseDTO;
import com.igriss.ListIn.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserMapper {

    private final LocationMapper locationMapper;

    public UserResponseDTO toUserResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getUserId())
                .nickName(user.getNickName())
                .enableCalling(user.getEnableCalling())
                .phoneNumber(user.getPhoneNumber())
                .fromTime(user.getFromTime())
                .toTime(user.getToTime())
                .email(user.getEmail())
                .followers(user.getFollowers())
                .following(user.getFollowing())
                .biography(user.getBiography())
                .profileImagePath(user.getProfileImagePath())
                .backgroundImagePath(user.getBackgroundImagePath())
                .rating(user.getRating())
                .isGrantedForPreciseLocation(user.getIsGrantedForPreciseLocation())
                .locationName(user.getLocationName())
                .country(locationMapper.toCountryDTO(user.getCountry()))
                .state(locationMapper.toStateDTO(user.getState()))
                .county(user.getCounty() != null ? locationMapper.toCountyDTO(user.getCounty()) : null)
                .longitude(user.getLongitude())
                .latitude(user.getLatitude())
                .role(user.getRole())
                .status(user.getStatus())
                .dateCreated(user.getDateCreated())
                .dateUpdated(user.getDateUpdated())
                .build();
    }

    public WSUserResponseDTO toWSUserResponseDTO(User user) {
        return WSUserResponseDTO.builder()
                .nickName(user.getNickName())
                .email(user.getEmail())
                .status(user.getStatus())
                .build();
    }

    public UserResponseDTO toUserResponseDTO(User user, Boolean following) {
        UserResponseDTO userResponseDTO = toUserResponseDTO(user);
        userResponseDTO.setIsFollowing(following);
        return userResponseDTO;
    }
}
