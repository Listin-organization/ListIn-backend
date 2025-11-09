package com.igriss.ListIn.chat.controller;

import com.igriss.ListIn.user.dto.WSUserResponseDTO;
import com.igriss.ListIn.user.enums.Status;
import com.igriss.ListIn.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatUserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private ChatUserController controller;

    private WSUserResponseDTO incomingUser;
    private WSUserResponseDTO serviceResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        incomingUser = WSUserResponseDTO.builder()
                .email("test@example.com")
                .nickName("TestUser")
                .status(Status.ONLINE)
                .build();

        serviceResponse = WSUserResponseDTO.builder()
                .email("test@example.com")
                .nickName("TestUser")
                .status(Status.ONLINE)
                .build();
    }

    @Test
    void connectUser_returnsResponseFromService() {

        when(userService.connect("test@example.com")).thenReturn(serviceResponse);

        WSUserResponseDTO result = controller.connectUser(incomingUser);

        assertNotNull(result);
        assertEquals("TestUser", result.getNickName());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(Status.ONLINE, result.getStatus());

        verify(userService).connect("test@example.com");
    }

    @Test
    void disconnectUser_returnsResponseFromService() {

        WSUserResponseDTO disconnectResponse = WSUserResponseDTO.builder()
                .email("test@example.com")
                .nickName("TestUser")
                .status(Status.OFFLINE)
                .build();

        when(userService.disconnect("test@example.com")).thenReturn(disconnectResponse);

        WSUserResponseDTO result = controller.disconnectUser(incomingUser);

        assertNotNull(result);
        assertEquals(Status.OFFLINE, result.getStatus());

        verify(userService).disconnect("test@example.com");
    }

    @Test
    void getConnectedUsers_returnsListOfUsers() {

        when(userService.findConnectedUsers()).thenReturn(List.of(serviceResponse));

        ResponseEntity<List<WSUserResponseDTO>> response =
                controller.getConnectedUsers();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("test@example.com", response.getBody().get(0).getEmail());

        verify(userService).findConnectedUsers();
    }
}
