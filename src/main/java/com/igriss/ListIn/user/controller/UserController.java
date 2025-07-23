package com.igriss.ListIn.user.controller;


import com.igriss.ListIn.publication.dto.page.PageResponse;
import com.igriss.ListIn.user.dto.FollowsDTO;
import com.igriss.ListIn.user.dto.FollowsResponseDTO;
import com.igriss.ListIn.user.dto.UpdateResponseDTO;
import com.igriss.ListIn.user.dto.UserRequestDTO;
import com.igriss.ListIn.user.dto.UserResponseDTO;
import com.igriss.ListIn.user.service.UserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserServiceImpl userService;

    @PatchMapping("/update")
    public UpdateResponseDTO updateProfile(@RequestBody UserRequestDTO userRequestDTO, HttpServletRequest request, Authentication authentication) {
        return userService.updateUserDetails(userRequestDTO, request, authentication);
    }

    @GetMapping()
    public UserResponseDTO getUserDetails(Authentication authentication) {
        return userService.getUserDetails(authentication);
    }

    @GetMapping("/{userId}")
    public UserResponseDTO getUserInfo(@PathVariable UUID userId, Authentication authentication) {
        return userService.findById(userId, authentication);
    }

    @GetMapping("/followers/{userId}")
    public ResponseEntity<PageResponse<FollowsResponseDTO>> getFollowers(@PathVariable UUID userId,
                                                                         @RequestParam(defaultValue = "0") int page,
                                                                         @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(userService.getFollowers(userId, page, size));
    }

    @GetMapping("/followings/{userId}")
    public ResponseEntity<PageResponse<FollowsResponseDTO>> getFollowings(@PathVariable UUID userId,
                                                                          @RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(userService.getFollowings(userId, page, size));
    }

    @GetMapping("/follow/{followingUserId}")
    public ResponseEntity<UserResponseDTO> follow(@PathVariable UUID followingUserId, Authentication authentication) throws BadRequestException {
        return ResponseEntity.ok(userService.followToUser(followingUserId, authentication));
    }

    @GetMapping("/unfollow/{followedUserId}")
    public ResponseEntity<UserResponseDTO> unFollow(@PathVariable UUID followedUserId, Authentication authentication) throws BadRequestException {
        return ResponseEntity.ok(userService.unFollowFromUser(followedUserId, authentication));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<PageResponse<FollowsResponseDTO>> getRecommendedUsers(@RequestParam(defaultValue = "0") int page,
                                                                             @RequestParam(defaultValue = "5") int size,
                                                                             Authentication connectedUser) {
        return ResponseEntity.ok(userService.getRecommendedUsers(connectedUser, page, size));
    }

}
