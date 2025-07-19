package com.igriss.ListIn.user.service;


import com.igriss.ListIn.publication.dto.PublicationRequestDTO;
import com.igriss.ListIn.publication.dto.page.PageResponse;
import com.igriss.ListIn.security.security_dto.ChangePasswordRequestDTO;
import com.igriss.ListIn.user.dto.FollowsResponseDTO;
import com.igriss.ListIn.user.dto.UpdateResponseDTO;
import com.igriss.ListIn.user.dto.UserRequestDTO;
import com.igriss.ListIn.user.dto.UserResponseDTO;
import com.igriss.ListIn.user.dto.WSUserResponseDTO;
import com.igriss.ListIn.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.BadRequestException;
import org.springframework.security.core.Authentication;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    Boolean existsByEmail(String email);

    void changePassword(ChangePasswordRequestDTO request, Principal connectedUser);

    Boolean isFollowingToUser(UUID followedUser, UUID followingUser);

    User getById(UUID id);

    User findByEmail(String username);

    UserResponseDTO findById(UUID id, Authentication authentication);

    UserResponseDTO getUserDetails(Authentication authentication);

    void updateContactDetails(PublicationRequestDTO request, User connectedUser);

    UpdateResponseDTO updateUserDetails(UserRequestDTO userRequestDTO, HttpServletRequest request, Authentication authentication);

    PageResponse<FollowsResponseDTO> getFollowers(UUID userId, int page, int size);

    PageResponse<FollowsResponseDTO> getFollowings(UUID userId, int page, int size);

    List<UUID> getFollowings(UUID userId);

    UserResponseDTO followToUser(UUID followingUserId, Authentication authentication) throws BadRequestException;

    UserResponseDTO unFollowFromUser(UUID followedUserId, Authentication authentication) throws BadRequestException;

    String storePhoneNumber(String userId, String phoneNumber);

    WSUserResponseDTO connect(String userEmail);

    WSUserResponseDTO disconnect(String userEmail);

    List<WSUserResponseDTO> findConnectedUsers();

    Optional<User> findUserByEmail(String email);
}
