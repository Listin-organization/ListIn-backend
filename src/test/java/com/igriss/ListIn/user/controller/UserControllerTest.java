package com.igriss.ListIn.user.controller;

import com.igriss.ListIn.publication.dto.page.PageResponse;
import com.igriss.ListIn.user.dto.FollowsResponseDTO;
import com.igriss.ListIn.user.dto.UpdateResponseDTO;
import com.igriss.ListIn.user.dto.UserRequestDTO;
import com.igriss.ListIn.user.dto.UserResponseDTO;
import com.igriss.ListIn.user.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUpdateProfile() {
        UserRequestDTO requestDTO = new UserRequestDTO();
        UpdateResponseDTO responseDTO = UpdateResponseDTO.builder()
                .updatedUserDetails(new UserResponseDTO())
                .build();

        when(userService.updateUserDetails(requestDTO, null, authentication)).thenReturn(responseDTO);

        UpdateResponseDTO result = userController.updateProfile(requestDTO, null, authentication);

        assertNotNull(result);
        assertEquals(responseDTO, result);
        verify(userService, times(1)).updateUserDetails(requestDTO, null, authentication);
    }

    @Test
    void testGetUserDetails() {
        UserResponseDTO responseDTO = new UserResponseDTO();
        when(userService.getUserDetails(authentication)).thenReturn(responseDTO);

        UserResponseDTO result = userController.getUserDetails(authentication);

        assertNotNull(result);
        assertEquals(responseDTO, result);
        verify(userService, times(1)).getUserDetails(authentication);
    }

    @Test
    void testGetUserInfo() {
        UUID userId = UUID.randomUUID();
        UserResponseDTO responseDTO = new UserResponseDTO();
        when(userService.findById(userId, authentication)).thenReturn(responseDTO);

        UserResponseDTO result = userController.getUserInfo(userId, authentication);

        assertNotNull(result);
        assertEquals(responseDTO, result);
        verify(userService, times(1)).findById(userId, authentication);
    }

    @Test
    void testGetFollowers() {
        UUID userId = UUID.randomUUID();
        FollowsResponseDTO followsResponse = new FollowsResponseDTO();
        PageResponse<FollowsResponseDTO> pageResponse = PageResponse.<FollowsResponseDTO>builder()
                .content(List.of(followsResponse))
                .build();

        when(userService.getFollowers(userId, 0, 5)).thenReturn(pageResponse);

        ResponseEntity<PageResponse<FollowsResponseDTO>> result = userController.getFollowers(userId, 0, 5);

        assertNotNull(result);
        assertEquals(pageResponse, result.getBody());
        verify(userService, times(1)).getFollowers(userId, 0, 5);
    }

    @Test
    void testGetFollowings() {
        UUID userId = UUID.randomUUID();
        FollowsResponseDTO followsResponse = new FollowsResponseDTO();
        PageResponse<FollowsResponseDTO> pageResponse = PageResponse.<FollowsResponseDTO>builder()
                .content(List.of(followsResponse))
                .build();

        when(userService.getFollowings(userId, 0, 5)).thenReturn(pageResponse);

        ResponseEntity<PageResponse<FollowsResponseDTO>> result = userController.getFollowings(userId, 0, 5);

        assertNotNull(result);
        assertEquals(pageResponse, result.getBody());
        verify(userService, times(1)).getFollowings(userId, 0, 5);
    }

    @Test
    void testFollow() throws Exception {
        UUID followingUserId = UUID.randomUUID();
        UserResponseDTO responseDTO = new UserResponseDTO();

        when(userService.followToUser(followingUserId, authentication)).thenReturn(responseDTO);

        ResponseEntity<UserResponseDTO> result = userController.follow(followingUserId, authentication);

        assertNotNull(result);
        assertEquals(responseDTO, result.getBody());
        verify(userService, times(1)).followToUser(followingUserId, authentication);
    }

    @Test
    void testUnFollow() throws Exception {
        UUID followedUserId = UUID.randomUUID();
        UserResponseDTO responseDTO = new UserResponseDTO();

        when(userService.unFollowFromUser(followedUserId, authentication)).thenReturn(responseDTO);

        ResponseEntity<UserResponseDTO> result = userController.unFollow(followedUserId, authentication);

        assertNotNull(result);
        assertEquals(responseDTO, result.getBody());
        verify(userService, times(1)).unFollowFromUser(followedUserId, authentication);
    }

    @Test
    void testGetRecommendedUsers() {
        FollowsResponseDTO followsResponse = new FollowsResponseDTO();
        PageResponse<FollowsResponseDTO> pageResponse = PageResponse.<FollowsResponseDTO>builder()
                .content(List.of(followsResponse))
                .build();

        when(userService.getRecommendedUsers(authentication, 0, 5)).thenReturn(pageResponse);

        ResponseEntity<PageResponse<FollowsResponseDTO>> result = userController.getRecommendedUsers(0, 5, authentication);

        assertNotNull(result);
        assertEquals(pageResponse, result.getBody());
        verify(userService, times(1)).getRecommendedUsers(authentication, 0, 5);
    }
}
