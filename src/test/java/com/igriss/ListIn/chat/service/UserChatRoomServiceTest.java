package com.igriss.ListIn.chat.service;

import com.igriss.ListIn.chat.dto.ChatMessageResponseDTO;
import com.igriss.ListIn.chat.dto.ChatRoomResponseDTO;
import com.igriss.ListIn.chat.entity.ChatMessage;
import com.igriss.ListIn.chat.entity.ChatRoom;
import com.igriss.ListIn.chat.mapper.ChatMessageMapper;
import com.igriss.ListIn.chat.mapper.ChatRoomMapper;
import com.igriss.ListIn.chat.repository.ChatRoomRepository;
import com.igriss.ListIn.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserChatRoomServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomMapper chatRoomMapper;

    @Mock
    private ChatMessageMapper messageMapper;

    @Mock
    private ChatMessageService chatMessageService;

    @InjectMocks
    private UserChatRoomsService service;

    private UUID userId;
    private ChatRoom chatRoom;
    private ChatRoomResponseDTO dto;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID();

        User sender = new User();
        sender.setUserId(userId);

        User recipient = new User();
        recipient.setUserId(UUID.randomUUID());

        chatRoom = ChatRoom.builder()
                .id(UUID.randomUUID())
                .chatRoomId("room-123")
                .sender(sender)
                .recipient(recipient)
                .unreadMessagesCount(3L)
                .messages(List.of())
                .build();

        dto = ChatRoomResponseDTO.builder()
                .chatRoomId("room-123")
                .recipientId(recipient.getUserId())
                .unreadMessages(3L)
                .build();
    }

    @Test
    void getUserChatRooms_setsLastMessage_whenPresent() {

        ChatMessage lastMessage = new ChatMessage();
        lastMessage.setContent("Last msg");

        ChatMessageResponseDTO lastMessageDTO = ChatMessageResponseDTO.builder()
                .content("Last msg")
                .build();

        when(chatRoomRepository.findBySender_UserId(userId))
                .thenReturn(List.of(chatRoom));

        when(chatRoomMapper.toDTO(chatRoom)).thenReturn(dto);

        when(chatMessageService.findLastMessage("room-123"))
                .thenReturn(Optional.of(lastMessage));

        when(messageMapper.toDTO(lastMessage)).thenReturn(lastMessageDTO);

        List<ChatRoomResponseDTO> result = service.getUserChatRooms(userId);

        assertEquals(1, result.size());
        assertNotNull(result.get(0).getLastMessage());
        assertEquals("Last msg", result.get(0).getLastMessage().getContent());

        verify(chatRoomRepository).findBySender_UserId(userId);
        verify(chatRoomMapper).toDTO(chatRoom);
        verify(chatMessageService).findLastMessage("room-123");
        verify(messageMapper).toDTO(lastMessage);
    }

    @Test
    void getUserChatRooms_noLastMessage_doesNotSetLastMessage() {

        when(chatRoomRepository.findBySender_UserId(userId))
                .thenReturn(List.of(chatRoom));

        when(chatRoomMapper.toDTO(chatRoom)).thenReturn(dto);

        when(chatMessageService.findLastMessage("room-123"))
                .thenReturn(Optional.empty());

        List<ChatRoomResponseDTO> result = service.getUserChatRooms(userId);

        assertEquals(1, result.size());
        assertNull(result.get(0).getLastMessage());

        verify(chatMessageService).findLastMessage("room-123");
        verify(chatRoomMapper).toDTO(chatRoom);
    }

    @Test
    void getUserChatRooms_multipleRooms_allProcessed() {

        ChatRoom chatRoom2 = ChatRoom.builder()
                .id(UUID.randomUUID())
                .chatRoomId("room-456")
                .sender(chatRoom.getSender())
                .recipient(chatRoom.getRecipient())
                .unreadMessagesCount(1L)
                .build();

        ChatRoomResponseDTO dto2 = ChatRoomResponseDTO.builder()
                .chatRoomId("room-456")
                .build();

        when(chatRoomRepository.findBySender_UserId(userId))
                .thenReturn(List.of(chatRoom, chatRoom2));

        when(chatRoomMapper.toDTO(chatRoom)).thenReturn(dto);
        when(chatRoomMapper.toDTO(chatRoom2)).thenReturn(dto2);

        when(chatMessageService.findLastMessage(anyString()))
                .thenReturn(Optional.empty());

        List<ChatRoomResponseDTO> result = service.getUserChatRooms(userId);

        assertEquals(2, result.size());
        assertNull(result.get(0).getLastMessage());
        assertNull(result.get(1).getLastMessage());

        verify(chatRoomMapper, times(2)).toDTO(any(ChatRoom.class));
        verify(chatMessageService, times(2)).findLastMessage(anyString());
    }
}
