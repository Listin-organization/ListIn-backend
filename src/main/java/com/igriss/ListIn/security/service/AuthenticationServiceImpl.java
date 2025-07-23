package com.igriss.ListIn.security.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igriss.ListIn.exceptions.UserHasAccountException;
import com.igriss.ListIn.exceptions.UserNotFoundException;
import com.igriss.ListIn.location.dto.LocationDTO;
import com.igriss.ListIn.location.service.LocationService;
import com.igriss.ListIn.security.security_dto.AuthenticationRequestDTO;
import com.igriss.ListIn.security.security_dto.AuthenticationResponseDTO;
import com.igriss.ListIn.security.security_dto.RegisterRequestDTO;
import com.igriss.ListIn.user.entity.User;
import com.igriss.ListIn.user.mapper.UserMapper;
import com.igriss.ListIn.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final LocationService locationService;

    public AuthenticationResponseDTO register(RegisterRequestDTO request, String language) throws UserHasAccountException {

        LocationDTO location = locationService.getLocation(request.getCountry(), request.getState(), request.getCounty(), language);

        var user = User.builder()
                .nickName(request.getNickName())
                .enableCalling(request.getEnableCalling())
                .phoneNumber(request.getPhoneNumber())
                .fromTime(request.getFromTime())
                .toTime(request.getToTime())
                .email(request.getEmail().toLowerCase())
                .biography(request.getBiography())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRoles())
                .isGrantedForPreciseLocation(request.getIsGrantedForPreciseLocation())
                .locationName(request.getLocationName())
                .country(location.getCountry())
                .state(location.getState())
                .county(location.getCounty())
                .longitude(request.getLongitude())
                .latitude(request.getLatitude())
                .build();

        try {
            userRepository.save(user);
            var jwtToken = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);
            return AuthenticationResponseDTO.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .userResponseDTO(userMapper.toUserResponseDTO(user))
                    .build();

        } catch (Exception exception) {
            log.warn("User with email '{}' already signed in", user.getEmail());
            throw new UserHasAccountException(String.format("User with email '%s' already signed in", user.getEmail()));
        }
    }

    public AuthenticationResponseDTO authenticate(AuthenticationRequestDTO request) throws UserNotFoundException {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase(),
                            request.getPassword()
                    )
            );
            var user = userRepository.findByEmail(request.getEmail().toLowerCase())
                    .orElseThrow();
            var jwtToken = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);

            return AuthenticationResponseDTO.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .userResponseDTO(userMapper.toUserResponseDTO(user))
                    .build();
        } catch (Exception exception) {
            log.warn("User with credentials: email '{}' does not exist", request.getEmail());
            throw new UserNotFoundException("Wrong email and/or password");
        }

    }

    public AuthenticationResponseDTO generateNewTokens(User updatedUser, HttpServletRequest request) {
        jwtService.blackListToken(getPreviousAccessToken(request));
        return AuthenticationResponseDTO.builder()
                .accessToken(jwtService.generateToken(updatedUser))
                .refreshToken(jwtService.generateRefreshToken(updatedUser))
                .userResponseDTO(userMapper.toUserResponseDTO(updatedUser))
                .build();
    }

    private String getPreviousAccessToken(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        return authHeader.substring(7);
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);

        //user should not have an access refreshing token with access token
        if (!jwtService.isRefreshToken(refreshToken)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }

        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            var user = this.userRepository.findByEmail(userEmail).orElseThrow();

            //if it is not, then we can go and create new access token using refresh token
            if (jwtService.isValidRefreshToken(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);

                var authResponse = AuthenticationResponseDTO.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .userResponseDTO(userMapper.toUserResponseDTO(user))
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

}
