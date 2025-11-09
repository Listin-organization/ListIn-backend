package com.igriss.ListIn.chat.controller;

import com.igriss.ListIn.chat.dto.ChatMessageRequestDTO;
import com.igriss.ListIn.chat.dto.ChatMessageResponseDTO;
import com.igriss.ListIn.chat.dto.DeliveryStatusUpdateDTO;
import com.igriss.ListIn.chat.dto.ViewConfirmationDTO;
import com.igriss.ListIn.chat.entity.ChatMessage;
import com.igriss.ListIn.chat.entity.ChatRoom;
import com.igriss.ListIn.chat.enums.DeliveryStatus;
import com.igriss.ListIn.chat.service.ChatMessageService;
import com.igriss.ListIn.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatControllerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ChatMessageService chatMessageService;

    @InjectMocks
    private ChatController controller;

    private UUID senderId;
    private UUID recipientId;
    private UUID messageId;
    private ChatMessage savedMsg;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        senderId = UUID.randomUUID();
        recipientId = UUID.randomUUID();
        messageId = UUID.randomUUID();

        User sender = new User();
        sender.setUserId(senderId);

        User recipient = new User();
        recipient.setUserId(recipientId);

        savedMsg = ChatMessage.builder()
                .id(messageId)
                .sender(sender)
                .recipient(recipient)
                .chatRoom(new ChatRoom())
                .content("Hello")
                .status(DeliveryStatus.DELIVERED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void processMessage_sendsMessagesToCorrectDestinations() {

        ChatMessageRequestDTO request = ChatMessageRequestDTO.builder()
                .id(messageId)
                .senderId(senderId)
                .recipientId(recipientId)
                .content("Hello")
                .build();

        when(chatMessageService.save(any())).thenReturn(savedMsg);

        controller.processMessage(request);

        verify(chatMessageService).save(argThat(r ->
                r.getSenderId().equals(senderId) &&
                        r.getRecipientId().equals(recipientId) &&
                        r.getContent().equals("Hello")
        ));

        verify(messagingTemplate).convertAndSendToUser(
                eq(recipientId.toString()),
                eq("/queue/messages"),
                argThat((ChatMessageResponseDTO res) ->
                        res.getId().equals(messageId) &&
                                res.getSenderId().equals(senderId) &&
                                res.getRecipientId().equals(recipientId) &&
                                res.getContent().equals("Hello") &&
                                res.getStatus() == DeliveryStatus.DELIVERED
                )
        );

        verify(messagingTemplate).convertAndSendToUser(
                eq(senderId.toString()),
                eq("/queue/messages/delivered"),
                any(ChatMessageResponseDTO.class)
        );
    }

    @Test
    void markMessagesAsViewed_updatesStatusAndNotifiesSender() {

        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID());

        ViewConfirmationDTO confirmation = ViewConfirmationDTO.builder()
                .senderId(senderId)
                .messageIds(ids)
                .build();

        controller.markMessagesAsViewed(confirmation);

        verify(chatMessageService).markMessagesAsViewed(ids);

        verify(messagingTemplate).convertAndSendToUser(
                eq(senderId.toString()),
                eq("/queue/messages/status"),
                argThat((DeliveryStatusUpdateDTO dto) ->
                        dto.getMessageIds().equals(ids) &&
                                dto.getStatus() == DeliveryStatus.VIEWED &&
                                dto.getUpdatedAt() != null
                )
        );
    }

    @Test
    void findChatMessages_returnsMessages() {

        ChatMessageResponseDTO responseDTO = ChatMessageResponseDTO.builder()
                .id(messageId)
                .senderId(senderId)
                .recipientId(recipientId)
                .content("Hello")
                .build();

        when(chatMessageService.findChatMessages(senderId, recipientId))
                .thenReturn(List.of(responseDTO));

        ResponseEntity<List<ChatMessageResponseDTO>> response =
                controller.findChatMessages(senderId, recipientId);

        assertNotNull(response);
        assertEquals(1, response.getBody().size());
        assertEquals(messageId, response.getBody().get(0).getId());

        verify(chatMessageService).findChatMessages(senderId, recipientId);
    }
}
