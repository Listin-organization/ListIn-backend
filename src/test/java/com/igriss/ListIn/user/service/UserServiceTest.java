package com.igriss.ListIn.user.service;

import com.igriss.ListIn.exceptions.BadRequestException;
import com.igriss.ListIn.exceptions.UserNotFoundException;
import com.igriss.ListIn.location.dto.LocationDTO;
import com.igriss.ListIn.location.service.LocationService;
import com.igriss.ListIn.publication.dto.PublicationRequestDTO;
import com.igriss.ListIn.publication.dto.page.PageResponse;
import com.igriss.ListIn.security.roles.Role;
import com.igriss.ListIn.security.security_dto.AuthenticationResponseDTO;
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
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Principal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserFollowerRepository userFollowerRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationServiceImpl authenticationService;

    @Mock
    private LocationService locationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExistsByEmail_ReturnsTrue() {
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        Boolean result = userService.existsByEmail(email);

        assertTrue(result);
        verify(userRepository, times(1)).existsByEmail(email);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void testExistsByEmail_ReturnsFalse() {
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        Boolean result = userService.existsByEmail(email);

        assertFalse(result);
        verify(userRepository, times(1)).existsByEmail(email);
        verifyNoMoreInteractions(userRepository);
    }


    @Test
    void changePassword_success() {
        User user = Instancio.create(User.class);
        user.setPassword("encodedCurrent");
        ChangePasswordRequestDTO request = ChangePasswordRequestDTO.builder()
                .currentPassword("current")
                .newPassword("newPassword")
                .confirmationPassword("newPassword")
                .build();

        Principal principal = new UsernamePasswordAuthenticationToken(user, null);

        when(passwordEncoder.matches("current", "encodedCurrent")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNew");

        userService.changePassword(request, principal);

        assertEquals("encodedNew", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_wrongCurrentPassword_throws() {
        User user = Instancio.create(User.class);
        user.setPassword("encodedCurrent");
        ChangePasswordRequestDTO request = ChangePasswordRequestDTO.builder()
                .currentPassword("wrong")
                .newPassword("new")
                .confirmationPassword("new")
                .build();
        Principal principal = new UsernamePasswordAuthenticationToken(user, null);

        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThrows(org.springframework.security.authentication.BadCredentialsException.class,
                () -> userService.changePassword(request, principal));
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_passwordsMismatch_throws() {
        User user = Instancio.create(User.class);
        user.setPassword("encodedCurrent");
        ChangePasswordRequestDTO request = ChangePasswordRequestDTO.builder()
                .currentPassword("current")
                .newPassword("new1")
                .confirmationPassword("new2")
                .build();
        Principal principal = new UsernamePasswordAuthenticationToken(user, null);

        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        assertThrows(org.springframework.security.authentication.BadCredentialsException.class,
                () -> userService.changePassword(request, principal));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateContactDetails_callsRepository() {
        User user = Instancio.create(User.class);
        PublicationRequestDTO request = new PublicationRequestDTO();
        request.setFromTime(LocalTime.of(9, 0));
        request.setToTime(LocalTime.of(18, 0));
        request.setPhoneNumber("+998901234567");
        request.setIsGrantedForPreciseLocation(true);

        userService.updateContactDetails(request, user);

        verify(userRepository).updateContactDetails(user.getUserId(),
                request.getFromTime(), request.getToTime(),
                request.getPhoneNumber(), request.getIsGrantedForPreciseLocation());
    }

    @Test
    void updateUserDetails_success() {
        User user = Instancio.create(User.class);
        user.setUserId(UUID.randomUUID());
        UserRequestDTO dto = new UserRequestDTO();
        dto.setNickName("Nick");
        dto.setIsBusinessAccount(true);

        LocationDTO location = Instancio.create(LocationDTO.class);
        when(locationService.getLocation(any(), any(), any(), any())).thenReturn(location);
        when(userRepository.updateUserDetails(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(1);
        when(userRepository.getUserByUserId(user.getUserId())).thenReturn(Optional.of(user));
        when(authenticationService.generateNewTokens(user, null)).thenReturn(null);
        when(userMapper.toUserResponseDTO(any(User.class))).thenReturn(null);

        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null);
        UpdateResponseDTO response = userService.updateUserDetails(dto, null, authentication);

        assertNotNull(response);
        verify(userRepository).updateUserRole(user.getUserId(), Role.BUSINESS_SELLER.name());
    }

    @Test
    void followToUser_success() throws Exception {
        User connected = Instancio.create(User.class);
        connected.setUserId(UUID.randomUUID());
        User target = Instancio.create(User.class);
        target.setUserId(UUID.randomUUID());
        target.setFollowers(5L);

        Authentication auth = new UsernamePasswordAuthenticationToken(connected, null);

        when(userRepository.findById(target.getUserId())).thenReturn(Optional.of(target));
        when(userFollowerRepository.existsById(any())).thenReturn(false);
        when(userRepository.incrementFollowingColumn(any())).thenReturn(1);
        when(userRepository.save(any())).thenReturn(target);
        when(userMapper.toUserResponseDTO(target, true)).thenReturn(null);
        when(userService.isFollowingToUser(any(), any())).thenReturn(true);

        userService.followToUser(target.getUserId(), auth);

        assertEquals(6L, target.getFollowers());
        verify(userFollowerRepository).save(any(UserFollower.class));
    }

    @Test
    void followToUser_followSelf_throws() {
        User user = Instancio.create(User.class);
        user.setUserId(UUID.randomUUID());
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null);

        assertThrows(Exception.class, () -> userService.followToUser(user.getUserId(), auth));
    }

    @Test
    void unFollowFromUser_success() throws Exception {
        User connected = Instancio.create(User.class);
        connected.setUserId(UUID.randomUUID());
        User target = Instancio.create(User.class);
        target.setUserId(UUID.randomUUID());
        target.setFollowers(5L);

        Authentication auth = new UsernamePasswordAuthenticationToken(connected, null);

        when(userRepository.findById(target.getUserId())).thenReturn(Optional.of(target));
        when(userFollowerRepository.existsById(any())).thenReturn(true);
        when(userRepository.decrementFollowingColumn(any())).thenReturn(1);
        when(userRepository.save(any())).thenReturn(target);
        when(userMapper.toUserResponseDTO(target, true)).thenReturn(null);
        when(userService.isFollowingToUser(any(), any())).thenReturn(false);

        userService.unFollowFromUser(target.getUserId(), auth);

        assertEquals(4L, target.getFollowers());
        verify(userFollowerRepository).deleteById(any());
    }

    @Test
    void isFollowingToUser_callsRepository() {
        UUID source = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        when(userFollowerRepository.existsByFollower_UserIdAndFollowing_UserId(source, target)).thenReturn(true);

        assertTrue(userService.isFollowingToUser(source, target));
    }

    @Test
    void findByEmail_success() {
        User user = Instancio.create(User.class);
        when(userRepository.findByEmail("email")).thenReturn(Optional.of(user));
        assertEquals(user, userService.findByEmail("email"));
    }

    @Test
    void findByEmail_notFound_throws() {
        when(userRepository.findByEmail("email")).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> userService.findByEmail("email"));
    }

    @Test
    void findUserByEmail_success() {
        User user = Instancio.create(User.class);
        when(userRepository.findByEmail("email")).thenReturn(Optional.of(user));
        Optional<User> result = userService.findUserByEmail("email");
        assertTrue(result.isPresent());
    }

    @Test
    void getUserDetails_callsMapper() {
        User user = Instancio.create(User.class);
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null);
        when(userMapper.toUserResponseDTO(user)).thenReturn(null);

        assertNull(userService.getUserDetails(auth));
    }

    @Test
    void storeAndGetPhoneNumber_success() {
        String token = "token";
        String phone = "+998901234567";
        String returnedToken = userService.storePhoneNumber(token, phone);
        assertEquals(token, returnedToken);
        assertEquals(phone, userService.getPhoneNumberByToken(token));
    }

    @Test
    void findConnectedUsers_callsRepository() {
        User user = Instancio.create(User.class);
        when(userRepository.findByStatus(Status.ONLINE)).thenReturn(List.of(user));
        when(userMapper.toWSUserResponseDTO(user)).thenReturn(null);

        assertEquals(1, userService.findConnectedUsers().size());
    }

    @Test
    void getFollowers_callsRepository() {
        UUID userId = UUID.randomUUID();

        FollowsDTO followsDTO = mock(FollowsDTO.class);
        when(followsDTO.getUserId()).thenReturn(UUID.randomUUID());
        when(followsDTO.getNickName()).thenReturn("Nick");
        when(followsDTO.getProfileImagePath()).thenReturn("profile.jpg");
        when(followsDTO.getFollowing()).thenReturn(10L);
        when(followsDTO.getFollowers()).thenReturn(5L);

        Page<FollowsDTO> page = new PageImpl<>(List.of(followsDTO));
        when(userFollowerRepository.findAllFollowers(userId, PageRequest.of(0, 10))).thenReturn(page);
        when(userService.isFollowingToUser(any(), any())).thenReturn(true);

        var result = userService.getFollowers(userId, 0, 10);
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Nick", result.getContent().get(0).getNickName());
    }

    @Test
    void getFollowings_callsRepository() {
        UUID userId = UUID.randomUUID();

        FollowsDTO followsDTO = mock(FollowsDTO.class);
        when(followsDTO.getUserId()).thenReturn(UUID.randomUUID());
        when(followsDTO.getNickName()).thenReturn("John");
        when(followsDTO.getProfileImagePath()).thenReturn("profile2.jpg");
        when(followsDTO.getFollowing()).thenReturn(20L);
        when(followsDTO.getFollowers()).thenReturn(8L);

        Page<FollowsDTO> page = new PageImpl<>(List.of(followsDTO));
        when(userFollowerRepository.findAllFollowings(userId, PageRequest.of(0, 10))).thenReturn(page);
        when(userService.isFollowingToUser(any(), any())).thenReturn(true);

        var result = userService.getFollowings(userId, 0, 10);
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("John", result.getContent().get(0).getNickName());
    }

    @Test
    void getFollowingsList_callsRepository() {
        UUID userId = UUID.randomUUID();
        when(userFollowerRepository.findFollowings(userId)).thenReturn(List.of(UUID.randomUUID()));

        assertEquals(1, userService.getFollowings(userId).size());
    }

    @Test
    void updateUserDetails_throwsInvalidUpdateStateException_whenStatusIsZero() {
        Authentication authentication = mock(Authentication.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        UserRequestDTO userRequestDTO = Instancio.create(UserRequestDTO.class);

        User mockUser = Instancio.create(User.class);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        LocationDTO location = Instancio.create(LocationDTO.class);
        when(locationService.getLocation(any(), any(), any(), any())).thenReturn(location);

        when(userRepository.updateUserDetails(
                any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(0);

        assertThrows(UserNotFoundException.class, () -> userService.updateUserDetails(userRequestDTO, request, authentication));
    }

    @Test
    void followToUser_throwsBadRequest_whenUserFollowsThemselves() {
        UUID userId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);
        User connectedUser = Instancio.create(User.class);
        connectedUser.setUserId(userId);
        when(auth.getPrincipal()).thenReturn(connectedUser);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userService.followToUser(userId, auth));

        assertEquals("User cannot follow themselves", ex.getMessage());
    }

    @Test
    void followToUser_throwsUserNotFound_whenTargetUserDoesNotExist() {
        UUID targetUserId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);
        User connectedUser = Instancio.create(User.class);
        connectedUser.setUserId(UUID.randomUUID());
        when(auth.getPrincipal()).thenReturn(connectedUser);

        when(userRepository.findById(targetUserId)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> userService.followToUser(targetUserId, auth));

        assertEquals("User to follow not found", ex.getMessage());
    }

    @Test
    void followToUser_throwsBadRequest_whenAlreadyFollowing() {
        UUID targetUserId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);
        User connectedUser = Instancio.create(User.class);
        connectedUser.setUserId(UUID.randomUUID());
        when(auth.getPrincipal()).thenReturn(connectedUser);

        User targetUser = Instancio.create(User.class);
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));
        UserFollower.UserFollowerId id = new UserFollower.UserFollowerId(connectedUser.getUserId(), targetUserId);
        when(userFollowerRepository.existsById(id)).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userService.followToUser(targetUserId, auth));

        assertEquals("Already following this user", ex.getMessage());
    }

    @Test
    void followToUser_throwsUserNotFound_whenIncrementFails() {
        UUID targetUserId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);
        User connectedUser = Instancio.create(User.class);
        connectedUser.setUserId(UUID.randomUUID());
        when(auth.getPrincipal()).thenReturn(connectedUser);

        User targetUser = Instancio.create(User.class);
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));
        UserFollower.UserFollowerId id = new UserFollower.UserFollowerId(connectedUser.getUserId(), targetUserId);
        when(userFollowerRepository.existsById(id)).thenReturn(false);
        when(userRepository.incrementFollowingColumn(connectedUser.getUserId())).thenReturn(0);

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> userService.followToUser(targetUserId, auth));

        assertEquals("Connected user not found", ex.getMessage());
    }

    @Test
    void unFollowFromUser_throwsBadRequest_whenUserUnfollowsThemselves() {
        UUID userId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);
        User connectedUser = Instancio.create(User.class);
        connectedUser.setUserId(userId);
        when(auth.getPrincipal()).thenReturn(connectedUser);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userService.unFollowFromUser(userId, auth));

        assertEquals("User cannot unfollow themselves", ex.getMessage());
    }

    @Test
    void unFollowFromUser_throwsUserNotFound_whenTargetUserDoesNotExist() {
        UUID targetUserId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);
        User connectedUser = Instancio.create(User.class);
        connectedUser.setUserId(UUID.randomUUID());
        when(auth.getPrincipal()).thenReturn(connectedUser);

        when(userRepository.findById(targetUserId)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> userService.unFollowFromUser(targetUserId, auth));

        assertEquals("User to unfollow not found", ex.getMessage());
    }

    @Test
    void unFollowFromUser_throwsBadRequest_whenNotFollowingUser() {
        UUID targetUserId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);
        User connectedUser = Instancio.create(User.class);
        connectedUser.setUserId(UUID.randomUUID());
        when(auth.getPrincipal()).thenReturn(connectedUser);

        User targetUser = Instancio.create(User.class);
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));
        UserFollower.UserFollowerId id = new UserFollower.UserFollowerId(connectedUser.getUserId(), targetUserId);
        when(userFollowerRepository.existsById(id)).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userService.unFollowFromUser(targetUserId, auth));

        assertEquals("Not following this user", ex.getMessage());
    }

    @Test
    void unFollowFromUser_throwsUserNotFound_whenDecrementFails() {
        UUID targetUserId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);
        User connectedUser = Instancio.create(User.class);
        connectedUser.setUserId(UUID.randomUUID());
        when(auth.getPrincipal()).thenReturn(connectedUser);

        User targetUser = Instancio.create(User.class);
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));
        UserFollower.UserFollowerId id = new UserFollower.UserFollowerId(connectedUser.getUserId(), targetUserId);
        when(userFollowerRepository.existsById(id)).thenReturn(true);
        when(userRepository.decrementFollowingColumn(connectedUser.getUserId())).thenReturn(0);

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> userService.unFollowFromUser(targetUserId, auth));

        assertEquals("Connected user not found", ex.getMessage());
    }

    @Test
    void testGetById_Success() {
        User user = Instancio.create(User.class);

        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));

        User byId = userService.getById(user.getUserId());

        assertEquals(byId.getUserId(), user.getUserId());
        verify(userRepository, times(1)).findById(user.getUserId());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void testGetById_NotFound() {
        UUID uuid = Instancio.create(UUID.class);
        when(userRepository.findById(uuid)).thenReturn(Optional.empty());

        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class, () -> userService.getById(uuid));

        assertEquals("User not found", userNotFoundException.getMessage());
        verify(userRepository, times(1)).findById(uuid);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void testUpdateUserDetails_StatusZero() {
        UserRequestDTO userRequestDTO = Instancio.create(UserRequestDTO.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        Authentication authentication = mock(Authentication.class);

        User connectedUser = Instancio.create(User.class);
        connectedUser.setUserId(UUID.randomUUID());

        when(authentication.getPrincipal()).thenReturn(connectedUser);

        LocationDTO locationDTO = Instancio.create(LocationDTO.class);
        when(locationService.getLocation(any(), any(), any(), any())).thenReturn(locationDTO);

        when(userRepository.updateUserDetails(
                any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(0);

        when(userRepository.getUserByUserId(any())).thenReturn(Optional.of(connectedUser));
        when(authenticationService.generateNewTokens(any(), any())).thenReturn(mock(AuthenticationResponseDTO.class));
        when(userMapper.toUserResponseDTO(any())).thenReturn(mock(UserResponseDTO.class));

        UpdateResponseDTO response = userService.updateUserDetails(userRequestDTO, request, authentication);

        assertNotNull(response);
        verify(userRepository).updateUserDetails(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any());
        verify(userRepository).updateUserRole(any(), any());
        verify(userRepository).getUserByUserId(any());
        verify(authenticationService).generateNewTokens(any(), any());
        verify(userMapper).toUserResponseDTO(any());
    }


    @Test
    void getRecommendedUsers_returnsPageResponse() {
        Authentication auth = mock(Authentication.class);
        User currentUser = Instancio.create(User.class);
        when(auth.getPrincipal()).thenReturn(currentUser);

        FollowsDTO followsDTO = mock(FollowsDTO.class);
        when(followsDTO.getUserId()).thenReturn(UUID.randomUUID());
        when(followsDTO.getNickName()).thenReturn("Nick");
        when(followsDTO.getProfileImagePath()).thenReturn("path");
        when(followsDTO.getFollowing()).thenReturn(1L);
        when(followsDTO.getFollowers()).thenReturn(2L);

        Page<FollowsDTO> page = new PageImpl<>(List.of(followsDTO));
        when(userRepository.findRecommendedUsers(eq(currentUser.getUserId()), any(PageRequest.class))).thenReturn(page);
        when(userFollowerRepository.existsByFollower_UserIdAndFollowing_UserId(any(), any())).thenReturn(true);

        PageResponse<FollowsResponseDTO> response = userService.getRecommendedUsers(auth, 0, 10);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(followsDTO.getNickName(), response.getContent().get(0).getNickName());
    }

    @Test
    void findById_returnsUserResponseDTO() {
        UUID userId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);
        User connectedUser = Instancio.create(User.class);
        connectedUser.setUserId(UUID.randomUUID());
        when(auth.getPrincipal()).thenReturn(connectedUser);

        User followedUser = Instancio.create(User.class);
        followedUser.setUserId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(followedUser));
        when(userFollowerRepository.existsByFollower_UserIdAndFollowing_UserId(any(), any())).thenReturn(true);
        when(userMapper.toUserResponseDTO(any(User.class), eq(true))).thenReturn(new UserResponseDTO());

        UserResponseDTO response = userService.findById(userId, auth);

        assertNotNull(response);
    }

    @Test
    void findById_throwsUserNotFoundException_whenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);
        User connectedUser = Instancio.create(User.class);
        when(auth.getPrincipal()).thenReturn(connectedUser);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> userService.findById(userId, auth));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void connect_setsStatusOnline_andReturnsWSUserResponseDTO() {
        String email = "test@example.com";
        User savedUser = Instancio.create(User.class);
        savedUser.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(savedUser));
        when(userMapper.toWSUserResponseDTO(savedUser)).thenReturn(new WSUserResponseDTO());

        WSUserResponseDTO response = userService.connect(email);

        assertNotNull(response);
        assertEquals(Status.ONLINE, savedUser.getStatus());
        verify(userRepository).save(savedUser);
    }

    @Test
    void connect_throwsUserNotFoundException_whenEmailNotFound() {
        String email = "missing@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class, () -> userService.connect(email));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void disconnect_setsStatusOffline_andReturnsWSUserResponseDTO() {
        String email = "test@example.com";
        User savedUser = Instancio.create(User.class);
        savedUser.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(savedUser));
        when(userMapper.toWSUserResponseDTO(savedUser)).thenReturn(new WSUserResponseDTO());

        WSUserResponseDTO response = userService.disconnect(email);

        assertNotNull(response);
        assertEquals(Status.OFFLINE, savedUser.getStatus());
        verify(userRepository).save(savedUser);
    }

    @Test
    void disconnect_throwsUserNotFoundException_whenEmailNotFound() {
        String email = "missing@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class, () -> userService.disconnect(email));
        assertEquals("User not found", ex.getMessage());
    }


}
