package com.igriss.ListIn.chat.controller;

import com.igriss.ListIn.chat.dto.ChatMessageResponseDTO;
import com.igriss.ListIn.chat.dto.ChatRoomResponseDTO;
import com.igriss.ListIn.chat.service.UserChatRoomsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class ChatRoomControllerTest {

    @Mock
    private UserChatRoomsService userChatRoomsService;

    @InjectMocks
    private ChatRoomController controller;

    private UUID userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
    }

    @Test
    void getChatRooms_returnsListOfChatRooms() {

        ChatRoomResponseDTO dto = ChatRoomResponseDTO.builder()
                .chatRoomId("room-123")
                .recipientId(UUID.randomUUID())
                .recipientImagePath("img.png")
                .recipientNickname("John")
                .unreadMessages(5L)
                .lastMessage(
                        ChatMessageResponseDTO.builder()
                                .content("Hello!")
                                .build()
                )
                .build();

        when(userChatRoomsService.getUserChatRooms(userId))
                .thenReturn(List.of(dto));

        ResponseEntity<List<ChatRoomResponseDTO>> response =
                controller.getChatRooms(userId);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("room-123", response.getBody().get(0).getChatRoomId());

        verify(userChatRoomsService).getUserChatRooms(userId);

        verifyNoMoreInteractions(userChatRoomsService);
    }
}
