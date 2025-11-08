package com.igriss.ListIn.chat.service;


import com.igriss.ListIn.chat.entity.ChatRoom;
import com.igriss.ListIn.chat.repository.ChatRoomRepository;
import com.igriss.ListIn.user.entity.User;
import com.igriss.ListIn.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatRoomServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ChatRoomService chatRoomService;

    private UUID senderId;
    private UUID recipientId;
    private User sender;
    private User recipient;
    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        senderId = UUID.randomUUID();
        recipientId = UUID.randomUUID();
        sender = User.builder().userId(senderId).build();
        recipient = User.builder().userId(recipientId).build();

        chatRoom = ChatRoom.builder()
                .id(UUID.randomUUID())
                .sender(sender)
                .recipient(recipient)
                .chatRoomId(senderId + "_" + recipientId)
                .unreadMessagesCount(0L)
                .build();
    }

    @Test
    void getChatRoom_ShouldReturnExistingChatRoom() {
        when(chatRoomRepository.findBySender_UserIdAndRecipient_UserId(senderId, recipientId))
                .thenReturn(Optional.of(chatRoom));

        Optional<ChatRoom> result = chatRoomService.getChatRoom(senderId, recipientId, true);

        assertTrue(result.isPresent());
        assertEquals(chatRoom, result.get());
        verify(chatRoomRepository, never()).save(any());
    }

    @Test
    void getChatRoom_ShouldCreateNewRoom_WhenNotFoundAndFlagTrue() {
        when(chatRoomRepository.findBySender_UserIdAndRecipient_UserId(senderId, recipientId))
                .thenReturn(Optional.empty());
        when(userService.getById(senderId)).thenReturn(sender);
        when(userService.getById(recipientId)).thenReturn(recipient);
        when(chatRoomRepository.save(any(ChatRoom.class))).thenAnswer(i -> i.getArgument(0));

        Optional<ChatRoom> result = chatRoomService.getChatRoom(senderId, recipientId, true);

        assertTrue(result.isPresent());
        verify(chatRoomRepository, times(2)).save(any(ChatRoom.class));
    }

    @Test
    void getChatRoom_ShouldReturnEmpty_WhenNotFoundAndFlagFalse() {
        when(chatRoomRepository.findBySender_UserIdAndRecipient_UserId(senderId, recipientId))
                .thenReturn(Optional.empty());

        Optional<ChatRoom> result = chatRoomService.getChatRoom(senderId, recipientId, false);

        assertTrue(result.isEmpty());
        verify(chatRoomRepository, never()).save(any());
    }

    @Test
    void getChatRoomById_ShouldReturnChatRoom() {
        when(chatRoomRepository.findById(chatRoom.getId()))
                .thenReturn(Optional.of(chatRoom));

        Optional<ChatRoom> result = chatRoomService.getChatRoomById(chatRoom.getId());

        assertTrue(result.isPresent());
        assertEquals(chatRoom.getChatRoomId(), result.get().getChatRoomId());
    }

    @Test
    void incrementUnreadCount_ShouldIncreaseUnreadMessages() {
        chatRoom.setUnreadMessagesCount(2L);

        chatRoomService.incrementUnreadCount(chatRoom);

        assertEquals(3L, chatRoom.getUnreadMessagesCount());
        verify(chatRoomRepository).save(chatRoom);
    }

    @Test
    void decrementUnreadCount_ShouldDecreaseUnreadMessages() {
        chatRoom.setUnreadMessagesCount(5L);

        chatRoomService.decrementUnreadCount(chatRoom, 2);

        assertEquals(3L, chatRoom.getUnreadMessagesCount());
        verify(chatRoomRepository).save(chatRoom);
    }

    @Test
    void decrementUnreadCount_ShouldNotGoBelowZero() {
        chatRoom.setUnreadMessagesCount(1L);

        chatRoomService.decrementUnreadCount(chatRoom, 5);

        assertEquals(0L, chatRoom.getUnreadMessagesCount());
        verify(chatRoomRepository).save(chatRoom);
    }
}
