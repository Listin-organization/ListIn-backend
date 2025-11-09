package com.igriss.ListIn.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FollowsResponseDTO {
    UUID userId;
    String nickName;
    String profileImagePath;
    Long following;
    Long followers;
    Boolean isFollowing;
}
