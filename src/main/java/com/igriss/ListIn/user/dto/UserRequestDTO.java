package com.igriss.ListIn.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.io.Serializable;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO implements Serializable {

    private String profileImagePath;

    private String backgroundImagePath;

    private String nickName;

    @Pattern(regexp = "^\\+998(\\s?\\d){9}$", message = "Phone Number must be in the format +998 00 123 45 67")
    private String phoneNumber;

    private Boolean isGrantedForPreciseLocation;

    private String locationName;

    private String country;

    private String state;

    private String county;

    private Double longitude;

    private Double latitude;

    private String biography;

    @JsonFormat(pattern = "HH:mm")
    @Builder.Default
    private LocalTime fromTime = LocalTime.of(0, 0);

    @JsonFormat(pattern = "HH:mm")
    @Builder.Default
    private LocalTime toTime = LocalTime.of(23, 59);

    private Boolean isBusinessAccount;

}
