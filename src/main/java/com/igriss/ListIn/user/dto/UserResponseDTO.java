package com.igriss.ListIn.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.igriss.ListIn.location.dto.CountryDTO;
import com.igriss.ListIn.location.dto.CountyDTO;
import com.igriss.ListIn.location.dto.StateDTO;
import com.igriss.ListIn.security.roles.Role;
import com.igriss.ListIn.user.enums.Status;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
//todo -> add a jakarta validation to each field
public class UserResponseDTO {

    private UUID id;

    private String nickName;

    private Boolean enableCalling;

    private String phoneNumber;

    private String biography;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime fromTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime toTime;

    private String email;

    private String profileImagePath;

    private String backgroundImagePath;

    private Float rating;

    private Boolean isGrantedForPreciseLocation;

    private String locationName;

    private CountryDTO country;

    private StateDTO state;

    private CountyDTO county;

    private Double longitude;

    private Long followers;

    private Long following;

    private Boolean isFollowing;

    private Double latitude;

    private Role role;

    private Status status;

    private LocalDateTime dateCreated;

    private LocalDateTime dateUpdated;

}
