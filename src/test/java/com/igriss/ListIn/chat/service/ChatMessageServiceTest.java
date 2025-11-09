package com.igriss.ListIn.chat.service;


import com.igriss.ListIn.chat.dto.ChatMessageRequestDTO;
import com.igriss.ListIn.chat.dto.ChatMessageResponseDTO;
import com.igriss.ListIn.chat.entity.ChatMessage;
import com.igriss.ListIn.chat.entity.ChatRoom;
import com.igriss.ListIn.chat.enums.DeliveryStatus;
import com.igriss.ListIn.chat.mapper.ChatMessageMapper;
import com.igriss.ListIn.chat.repository.ChatMessageRepository;
import com.igriss.ListIn.exceptions.ResourceNotFoundException;
import com.igriss.ListIn.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatRoomService chatRoomService;

    @Mock
    private ChatMessageMapper chatMessageMapper;

    @InjectMocks
    private ChatMessageService chatMessageService;

    private User sender;
    private User recipient;
    private ChatRoom chatRoom1;
    private ChatRoom chatRoom2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sender = User.builder().userId(UUID.randomUUID()).build();
        recipient = User.builder().userId(UUID.randomUUID()).build();

        chatRoom1 = ChatRoom.builder().id(UUID.randomUUID()).sender(sender).recipient(recipient).build();
        chatRoom2 = ChatRoom.builder().id(UUID.randomUUID()).sender(recipient).recipient(sender).build();
    }

    @Test
    void save_ShouldSaveMessagesSuccessfully() {
        // Given
        ChatMessageRequestDTO request = new ChatMessageRequestDTO();
        request.setSenderId(sender.getUserId());
        request.setRecipientId(recipient.getUserId());
        request.setContent("Hello!");

        when(chatRoomService.getChatRoom(sender.getUserId(), recipient.getUserId(), true))
                .thenReturn(Optional.of(chatRoom1));
        when(chatRoomService.getChatRoom(recipient.getUserId(), sender.getUserId(), true))
                .thenReturn(Optional.of(chatRoom2));

        when(chatMessageRepository.save(any(ChatMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ChatMessage savedMessage = chatMessageService.save(request);

        // Then
        assertEquals("Hello!", savedMessage.getContent());
        assertEquals(DeliveryStatus.DELIVERED, savedMessage.getStatus());
        verify(chatRoomService).incrementUnreadCount(chatRoom2);
        verify(chatMessageRepository, times(2)).save(any(ChatMessage.class));
    }

    @Test
    void save_ShouldThrowException_WhenChatRoomNotFound() {
        ChatMessageRequestDTO request = new ChatMessageRequestDTO();
        request.setSenderId(sender.getUserId());
        request.setRecipientId(recipient.getUserId());

        when(chatRoomService.getChatRoom(any(), any(), anyBoolean()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> chatMessageService.save(request));
    }

    @Test
    void findChatMessages_ShouldReturnMappedMessages() {
        UUID senderId = sender.getUserId();
        UUID recipientId = recipient.getUserId();

        ChatMessage msg = ChatMessage.builder().content("hi").chatRoom(chatRoom1).build();
        ChatMessageResponseDTO dto = new ChatMessageResponseDTO();

        when(chatRoomService.getChatRoom(senderId, recipientId, false)).thenReturn(Optional.of(chatRoom1));
        when(chatMessageRepository.findByChatRoom_Id(chatRoom1.getId())).thenReturn(List.of(msg));
        when(chatMessageMapper.toDTO(msg)).thenReturn(dto);

        List<ChatMessageResponseDTO> result = chatMessageService.findChatMessages(senderId, recipientId);

        assertEquals(1, result.size());
        verify(chatMessageRepository).findByChatRoom_Id(chatRoom1.getId());
    }

    @Test
    void findChatMessages_ShouldReturnEmptyList_WhenNoChatRoomFound() {
        when(chatRoomService.getChatRoom(any(), any(), anyBoolean()))
                .thenReturn(Optional.empty());

        List<ChatMessageResponseDTO> result = chatMessageService.findChatMessages(UUID.randomUUID(), UUID.randomUUID());

        assertTrue(result.isEmpty());
        verify(chatMessageRepository, never()).findByChatRoom_Id(any());
    }

    @Test
    void findLastMessage_ShouldReturnOptionalMessage() {
        ChatMessage message = ChatMessage.builder().id(UUID.randomUUID()).content("last msg").build();
        when(chatMessageRepository.findTopByChatRoom_ChatRoomIdOrderByCreatedAtDesc("room123"))
                .thenReturn(Optional.of(message));

        Optional<ChatMessage> result = chatMessageService.findLastMessage("room123");

        assertTrue(result.isPresent());
        assertEquals("last msg", result.get().getContent());
    }

    @Test
    void markMessagesAsViewed_nullMessageIds_shouldDoNothing() {
        chatMessageService.markMessagesAsViewed(null);

        verifyNoInteractions(chatMessageRepository);
        verifyNoInteractions(chatRoomService);
    }

    @Test
    void markMessagesAsViewed_emptyList_shouldDoNothing() {
        chatMessageService.markMessagesAsViewed(Collections.emptyList());

        verifyNoInteractions(chatMessageRepository);
        verifyNoInteractions(chatRoomService);
    }

    @Test
    void markMessagesAsViewed_shouldMarkOriginalAndReflectionAndDecrementUnread() {

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        LocalDateTime now = LocalDateTime.now();

        ChatRoom room1 = ChatRoom.builder().id(UUID.randomUUID()).build();
        ChatRoom room2 = ChatRoom.builder().id(UUID.randomUUID()).build();

        ChatMessage m1 = ChatMessage.builder()
                .id(id1)
                .content("hello")
                .createdAt(now)
                .sender(new com.igriss.ListIn.user.entity.User())
                .recipient(new com.igriss.ListIn.user.entity.User())
                .chatRoom(room1)
                .status(DeliveryStatus.DELIVERED)
                .build();

        ChatMessage m2 = ChatMessage.builder()
                .id(id2)
                .content("hi")
                .createdAt(now)
                .sender(m1.getSender())
                .recipient(m1.getRecipient())
                .chatRoom(room2)
                .status(DeliveryStatus.DELIVERED)
                .build();

        when(chatMessageRepository.findAllById(anyList()))
                .thenReturn(List.of(m1, m2));

        ChatMessage refl1 = ChatMessage.builder()
                .id(UUID.randomUUID())
                .content("hello")
                .createdAt(now)
                .sender(m1.getSender())
                .recipient(m1.getRecipient())
                .chatRoom(room1)
                .build();

        ChatMessage refl2 = ChatMessage.builder()
                .id(UUID.randomUUID())
                .content("hi")
                .createdAt(now)
                .sender(m2.getSender())
                .recipient(m2.getRecipient())
                .chatRoom(room2)
                .build();

        when(chatMessageRepository.findByContentAndCreatedAtAndSenderAndRecipient(
                eq("hello"), eq(now), eq(m1.getSender()), eq(m1.getRecipient())))
                .thenReturn(List.of(m1, refl1));

        when(chatMessageRepository.findByContentAndCreatedAtAndSenderAndRecipient(
                eq("hi"), eq(now), eq(m2.getSender()), eq(m2.getRecipient())))
                .thenReturn(List.of(m2, refl2));

        when(chatRoomService.getChatRoomById(room1.getId()))
                .thenReturn(Optional.of(room1));

        when(chatRoomService.getChatRoomById(room2.getId()))
                .thenReturn(Optional.of(room2));

        chatMessageService.markMessagesAsViewed(List.of(id1, id2));

        assertThat(m1.getStatus()).isEqualTo(DeliveryStatus.VIEWED);
        assertThat(m2.getStatus()).isEqualTo(DeliveryStatus.VIEWED);

        assertThat(refl1.getStatus()).isEqualTo(DeliveryStatus.VIEWED);
        assertThat(refl2.getStatus()).isEqualTo(DeliveryStatus.VIEWED);


        verify(chatRoomService).decrementUnreadCount(room1, 1L);
        verify(chatRoomService).decrementUnreadCount(room2, 1L);
    }

    @Test
    void markMessagesAsViewed_whenReflectionNotFound_shouldSkipReflection() {

        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        ChatRoom room = ChatRoom.builder().id(UUID.randomUUID()).build();

        ChatMessage m = ChatMessage.builder()
                .id(id)
                .content("test")
                .createdAt(now)
                .sender(new com.igriss.ListIn.user.entity.User())
                .recipient(new com.igriss.ListIn.user.entity.User())
                .chatRoom(room)
                .status(DeliveryStatus.DELIVERED)
                .build();

        when(chatMessageRepository.findAllById(anyList()))
                .thenReturn(List.of(m));

        when(chatMessageRepository.findByContentAndCreatedAtAndSenderAndRecipient(
                anyString(), any(), any(), any()))
                .thenReturn(List.of(m));

        when(chatRoomService.getChatRoomById(room.getId()))
                .thenReturn(Optional.of(room));

        chatMessageService.markMessagesAsViewed(List.of(id));

        assertThat(m.getStatus()).isEqualTo(DeliveryStatus.VIEWED);

        verify(chatRoomService).decrementUnreadCount(room, 1L);
    }
}
