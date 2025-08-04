package com.igriss.ListIn.chat.controller;

import com.igriss.ListIn.chat.dto.ChatMessageRequestDTO;
import com.igriss.ListIn.chat.dto.ChatMessageResponseDTO;
import com.igriss.ListIn.chat.dto.DeliveryStatusUpdateDTO;
import com.igriss.ListIn.chat.dto.ViewConfirmationDTO;
import com.igriss.ListIn.chat.entity.ChatMessage;
import com.igriss.ListIn.chat.enums.DeliveryStatus;
import com.igriss.ListIn.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessageRequestDTO request) {
        ChatMessage savedMsg = chatMessageService.save(request);

        ChatMessageResponseDTO response = ChatMessageResponseDTO.builder()
                .id(savedMsg.getId())
                .senderId(savedMsg.getSender().getUserId())
                .recipientId(savedMsg.getRecipient().getUserId())
                .content(savedMsg.getContent())
                .status(savedMsg.getStatus())
                .sentAt(savedMsg.getCreatedAt())
                .updatedAt(savedMsg.getUpdatedAt())
                .build();

        messagingTemplate.convertAndSendToUser(request.getRecipientId().toString(), "/queue/messages", response);
        log.info("Sent message: {}", response);

        messagingTemplate.convertAndSendToUser(request.getSenderId().toString(), "/queue/messages/delivered", response);
        log.info("Sent message back to the sender: {}", response);
    }

    // New endpoint to mark messages as viewed
    @MessageMapping("/chat/view")
    public void markMessagesAsViewed(@Payload ViewConfirmationDTO confirmation) {
        chatMessageService.markMessagesAsViewed(confirmation.getMessageIds());

        // Notify the original sender that their messages were viewed
        // First, create a response object
        DeliveryStatusUpdateDTO response = DeliveryStatusUpdateDTO.builder()
                .messageIds(confirmation.getMessageIds())
                .status(DeliveryStatus.VIEWED)
                .updatedAt(LocalDateTime.now())
                .build();

        // Send the viewed confirmation to the original sender
        messagingTemplate.convertAndSendToUser(confirmation.getSenderId().toString(), "/queue/messages/status", response);
        log.info("Marked viewed messages as viewed: {}", confirmation.getMessageIds());
    }


    @GetMapping("/messages/{senderId}/{recipientId}")
    public ResponseEntity<List<ChatMessageResponseDTO>> findChatMessages(@PathVariable UUID senderId, @PathVariable UUID recipientId) {
        return ResponseEntity.ok(chatMessageService.findChatMessages(senderId, recipientId));
    }

}
