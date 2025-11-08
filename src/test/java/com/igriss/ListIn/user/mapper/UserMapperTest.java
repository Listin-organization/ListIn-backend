package com.igriss.ListIn.user.mapper;

import com.igriss.ListIn.location.mapper.LocationMapper;
import com.igriss.ListIn.security.roles.Role;
import com.igriss.ListIn.user.dto.UserResponseDTO;
import com.igriss.ListIn.user.dto.WSUserResponseDTO;
import com.igriss.ListIn.user.entity.User;
import com.igriss.ListIn.user.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserMapperTest {

    @InjectMocks
    private UserMapper userMapper;

    @Mock
    private LocationMapper locationMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private User createMockUser() {
        return User.builder()
                .userId(UUID.randomUUID())
                .nickName("John Doe")
                .enableCalling(true)
                .phoneNumber("+998901234567")
                .fromTime(LocalTime.of(9, 0))
                .toTime(LocalTime.of(18, 0))
                .email("john@example.com")
                .biography("Bio")
                .password("password")
                .profileImagePath("/images/profile.jpg")
                .backgroundImagePath("/images/bg.jpg")
                .rating(4.5F)
                .followers(100L)
                .following(50L)
                .isGrantedForPreciseLocation(true)
                .locationName("Tashkent")
                .country(null)
                .state(null)
                .county(null)
                .longitude(69.22)
                .latitude(41.31)
                .role(Role.ADMIN)
                .status(Status.OFFLINE)
                .dateCreated(LocalDateTime.now())
                .dateUpdated(LocalDateTime.now())
                .build();
    }

    @Test
    void testToUserResponseDTO() {
        User user = createMockUser();
        when(locationMapper.toCountryDTO(user.getCountry())).thenReturn(null);
        when(locationMapper.toStateDTO(user.getState())).thenReturn(null);

        UserResponseDTO dto = userMapper.toUserResponseDTO(user);

        assertNotNull(dto);
        assertEquals(user.getUserId(), dto.getId());
        assertEquals(user.getNickName(), dto.getNickName());
        assertEquals(user.getEnableCalling(), dto.getEnableCalling());
        assertEquals(user.getPhoneNumber(), dto.getPhoneNumber());
        assertEquals(user.getFromTime(), dto.getFromTime());
        assertEquals(user.getToTime(), dto.getToTime());
        assertEquals(user.getEmail(), dto.getEmail());
        assertEquals(user.getFollowers(), dto.getFollowers());
        assertEquals(user.getFollowing(), dto.getFollowing());
        assertEquals(user.getBiography(), dto.getBiography());
        assertEquals(user.getProfileImagePath(), dto.getProfileImagePath());
        assertEquals(user.getBackgroundImagePath(), dto.getBackgroundImagePath());
        assertEquals(user.getRating(), dto.getRating());
        assertEquals(user.getIsGrantedForPreciseLocation(), dto.getIsGrantedForPreciseLocation());
        assertEquals(user.getLocationName(), dto.getLocationName());
        assertNull(dto.getCountry());
        assertNull(dto.getState());
        assertNull(dto.getCounty());
        assertEquals(user.getLongitude(), dto.getLongitude());
        assertEquals(user.getLatitude(), dto.getLatitude());
        assertEquals(user.getRole(), dto.getRole());
        assertEquals(user.getStatus(), dto.getStatus());
        assertEquals(user.getDateCreated(), dto.getDateCreated());
        assertEquals(user.getDateUpdated(), dto.getDateUpdated());

        verify(locationMapper, times(1)).toCountryDTO(user.getCountry());
        verify(locationMapper, times(1)).toStateDTO(user.getState());
    }

    @Test
    void testToWSUserResponseDTO() {
        User user = createMockUser();

        WSUserResponseDTO dto = userMapper.toWSUserResponseDTO(user);

        assertNotNull(dto);
        assertEquals(user.getNickName(), dto.getNickName());
        assertEquals(user.getEmail(), dto.getEmail());
        assertEquals(user.getStatus(), dto.getStatus());
    }

    @Test
    void testToUserResponseDTOWithFollowing() {
        User user = createMockUser();
        Boolean following = true;
        when(locationMapper.toCountryDTO(user.getCountry())).thenReturn(null);
        when(locationMapper.toStateDTO(user.getState())).thenReturn(null);

        UserResponseDTO dto = userMapper.toUserResponseDTO(user, following);

        assertNotNull(dto);
        assertTrue(dto.getIsFollowing());
        assertEquals(user.getNickName(), dto.getNickName());

        verify(locationMapper, times(1)).toCountryDTO(user.getCountry());
        verify(locationMapper, times(1)).toStateDTO(user.getState());
    }
}
