package com.igriss.ListIn.user.service;


import com.igriss.ListIn.exceptions.UserNotFoundException;
import com.igriss.ListIn.location.dto.LocationDTO;
import com.igriss.ListIn.location.service.LocationService;
import com.igriss.ListIn.publication.dto.PublicationRequestDTO;
import com.igriss.ListIn.publication.dto.page.PageResponse;
import com.igriss.ListIn.security.roles.Role;
import com.igriss.ListIn.security.security_dto.ChangePasswordRequestDTO;
import com.igriss.ListIn.security.service.AuthenticationServiceImpl;
import com.igriss.ListIn.user.dto.FollowsDTO;
import com.igriss.ListIn.user.dto.FollowsResponseDTO;
import com.igriss.ListIn.user.dto.UpdateResponseDTO;
import com.igriss.ListIn.user.dto.UserRequestDTO;
import com.igriss.ListIn.user.dto.UserResponseDTO;
import com.igriss.ListIn.user.dto.WSUserResponseDTO;
import com.igriss.ListIn.user.entity.User;
import com.igriss.ListIn.user.entity.UserFollower;
import com.igriss.ListIn.user.enums.Status;
import com.igriss.ListIn.user.mapper.UserMapper;
import com.igriss.ListIn.user.repository.UserFollowerRepository;
import com.igriss.ListIn.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final AuthenticationServiceImpl authenticationService;
    private final LocationService locationService;
    private final UserFollowerRepository userFollowerRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private final Map<String, String> phoneTokenMap = new HashMap<>();

    @Override
    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override // todo -> will be fixed the logical bug
    public void changePassword(ChangePasswordRequestDTO request, Principal connectedUser) {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        //check if users password is correct
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            var exception = new BadCredentialsException("Wrong password");
            log.error("Current password is incorrect", exception);
            throw exception;
        }
        //check if the confirmation password matches with the new password
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            var exception = new BadCredentialsException("Passwords does not match");
            log.error("Passwords does not match", exception);
            throw exception;
        }
        //update the password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        //save user back to database
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateContactDetails(PublicationRequestDTO request, User connectedUser) {
        userRepository.updateContactDetails(
                connectedUser.getUserId(),
                request.getFromTime(),
                request.getToTime(),
                request.getPhoneNumber(),
                request.getIsGrantedForPreciseLocation()
        );
    }


    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UpdateResponseDTO updateUserDetails(UserRequestDTO userRequestDTO, HttpServletRequest request, Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        log.info("countyName {}", userRequestDTO.getCounty());
        log.info("countyName {}", userRequestDTO.getCounty());
        log.info("stateName {}", userRequestDTO.getState());

        LocationDTO location = locationService.getLocation(userRequestDTO.getCountry(), userRequestDTO.getState(), userRequestDTO.getCounty(), "ru");

        log.info("country {}", location.getCountry());
        log.info("county {}", location.getCounty());
        log.info("state {}", location.getState());

        Integer status = userRepository.updateUserDetails(
                user.getUserId(),
                userRequestDTO.getNickName(),
                userRequestDTO.getProfileImagePath(),
                userRequestDTO.getBackgroundImagePath(),
                userRequestDTO.getPhoneNumber(),
                userRequestDTO.getIsGrantedForPreciseLocation(),
                userRequestDTO.getLocationName(),
                location.getCountry() != null ? location.getCountry().getId() : null,
                location.getState() != null ? location.getState().getId() : null,
                location.getCounty() != null ? location.getCounty().getId() : null,
                userRequestDTO.getLongitude(),
                userRequestDTO.getLatitude(),
                userRequestDTO.getFromTime(),
                userRequestDTO.getToTime(),
                userRequestDTO.getBiography()
        );

        userRepository.updateUserRole(user.getUserId(), userRequestDTO.getIsBusinessAccount() ? Role.BUSINESS_SELLER.name() : Role.INDIVIDUAL_SELLER.name());

        User updatedUser = userRepository.getUserByUserId(user.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (status != 0) log.info("User updated: {}", user);
        else log.info("User update failed: {}", user);

        return UpdateResponseDTO.builder()
                .tokens(
                        authenticationService.generateNewTokens(updatedUser, request)
                )
                .updatedUserDetails(
                        userMapper.toUserResponseDTO(status != 0 ? updatedUser : user)
                ).build();
    }

    @Override
    public PageResponse<FollowsResponseDTO> getFollowers(UUID userId, int page, int size) {

        Page<FollowsDTO> allFollowers = userFollowerRepository.findAllFollowers(userId, PageRequest.of(page, size));

        return getFollowsDTOPageResponse(userId, allFollowers);
    }

    @Override
    public PageResponse<FollowsResponseDTO> getFollowings(UUID userId, int page, int size) {

        Page<FollowsDTO> allFollowings = userFollowerRepository.findAllFollowings(userId, PageRequest.of(page, size));

        return getFollowsDTOPageResponse(userId, allFollowings);
    }

    @Override
    public List<UUID> getFollowings(UUID userId) {
        return userFollowerRepository.findFollowings(userId);
    }


    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserResponseDTO followToUser(UUID followingUserId, Authentication authentication) throws BadRequestException {
        User connectedUser = (User) authentication.getPrincipal();

        if (connectedUser.getUserId().equals(followingUserId)) {
            throw new BadRequestException("User cannot follow themselves");
        }


        User userToBeFollowed = userRepository.findById(followingUserId)
                .orElseThrow(() -> new UserNotFoundException("User to follow not found"));

        UserFollower.UserFollowerId id = new UserFollower.UserFollowerId(connectedUser.getUserId(), followingUserId);

        if (userFollowerRepository.existsById(id)) {
            throw new BadRequestException("Already following this user");
        }

        Integer isUpdated = userRepository.incrementFollowingColumn(connectedUser.getUserId());
        if (isUpdated == 0) {
            throw new UserNotFoundException("Connected user not found");
        }

        userToBeFollowed.setFollowers(userToBeFollowed.getFollowers() + 1);
        userRepository.save(userToBeFollowed);

        UserFollower userFollower = new UserFollower(connectedUser, userToBeFollowed);
        userFollowerRepository.save(userFollower);

        return userMapper.toUserResponseDTO(userToBeFollowed, isFollowingToUser(connectedUser.getUserId(), userToBeFollowed.getUserId()));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserResponseDTO unFollowFromUser(UUID followedUserId, Authentication authentication) throws BadRequestException {
        User connectedUser = (User) authentication.getPrincipal();

        if (connectedUser.getUserId().equals(followedUserId)) {
            throw new BadRequestException("User cannot unfollow themselves");
        }

        User userToBeUnFollowed = userRepository.findById(followedUserId)
                .orElseThrow(() -> new UserNotFoundException("User to unfollow not found"));

        UserFollower.UserFollowerId id = new UserFollower.UserFollowerId(connectedUser.getUserId(), followedUserId);

        if (!userFollowerRepository.existsById(id)) {
            throw new BadRequestException("Not following this user");
        }

        Integer isUpdated = userRepository.decrementFollowingColumn(connectedUser.getUserId());
        if (isUpdated == 0) {
            throw new UserNotFoundException("Connected user not found");
        }

        userToBeUnFollowed.setFollowers(userToBeUnFollowed.getFollowers() - 1);
        userRepository.save(userToBeUnFollowed);

        userFollowerRepository.deleteById(id);

        return userMapper.toUserResponseDTO(userToBeUnFollowed, isFollowingToUser(connectedUser.getUserId(), userToBeUnFollowed.getUserId()));
    }

    @Override
    public Boolean isFollowingToUser(UUID sourceUser, UUID targetUser) {
        return userFollowerRepository.existsByFollower_UserIdAndFollowing_UserId(sourceUser, targetUser);
    }

    @Override
    public User getById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Override
    public User findByEmail(String username) {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public Optional<User> findUserByEmail(String username) {
        return userRepository.findByEmail(username);
    }

    @Override
    public UserResponseDTO findById(UUID userId, Authentication authentication) {

        User followedUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        User connectedUser = (User) authentication.getPrincipal();

        Boolean followingToUser = isFollowingToUser(followedUser.getUserId(), connectedUser.getUserId());

        return userMapper.toUserResponseDTO(userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found")), followingToUser);
    }

    public UserResponseDTO getUserDetails(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return userMapper.toUserResponseDTO(user);
    }

    public String storePhoneNumber(String userId, String phoneNumber) {

        phoneTokenMap.put(userId, phoneNumber);
        return userId;
    }

    @Override
    public WSUserResponseDTO connect(String userEmail) {
        User savedUser = userRepository.findByEmail(userEmail).orElseThrow(() -> new UserNotFoundException("User not found"));
        savedUser.setStatus(Status.ONLINE);
        userRepository.save(savedUser);
        return userMapper.toWSUserResponseDTO(savedUser);
    }

    @Override
    public WSUserResponseDTO disconnect(String userEmail) {
        User savedUser = userRepository.findByEmail(userEmail).orElseThrow(() -> new UserNotFoundException("User not found"));
        savedUser.setStatus(Status.OFFLINE);
        userRepository.save(savedUser);
        return userMapper.toWSUserResponseDTO(savedUser);
    }

    @Override
    public List<WSUserResponseDTO> findConnectedUsers() {
        return userRepository.findByStatus(Status.ONLINE).stream().map(userMapper::toWSUserResponseDTO).toList();
    }

    public String getPhoneNumberByToken(String token) {
        return phoneTokenMap.get(token);
    }

    private PageResponse<FollowsResponseDTO> getFollowsDTOPageResponse(UUID userId, Page<FollowsDTO> allFollowings) {

        List<FollowsResponseDTO> followsResponseDTOS = allFollowings.getContent().stream()
                .map(followsDTO -> FollowsResponseDTO.builder()
                        .userId(followsDTO.getUserId())
                        .nickName(followsDTO.getNickName())
                        .profileImagePath(followsDTO.getProfileImagePath())
                        .following(followsDTO.getFollowing())
                        .followers(followsDTO.getFollowers())
                        .isFollowing(isFollowingToUser(userId, followsDTO.getUserId()))
                        .build()
                ).toList();

        return PageResponse.<FollowsResponseDTO>builder()
                .content(followsResponseDTOS)
                .number(allFollowings.getNumber())
                .size(allFollowings.getSize())
                .totalElements(allFollowings.getTotalElements())
                .totalPages(allFollowings.getTotalPages())
                .first(allFollowings.isFirst())
                .last(allFollowings.isLast())
                .build();
    }
}
