package com.igriss.ListIn.chat.mapper;

import com.igriss.ListIn.chat.dto.ChatMessageResponseDTO;
import com.igriss.ListIn.chat.entity.ChatMessage;
import com.igriss.ListIn.chat.entity.ChatRoom;
import com.igriss.ListIn.chat.enums.DeliveryStatus;
import com.igriss.ListIn.user.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ChatMessageMapperTest {

    private final ChatMessageMapper mapper = new ChatMessageMapper();

    @Test
    void toDTO_mapsAllFieldsCorrectly() {
        UUID messageId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();

        User sender = new User();
        sender.setUserId(senderId);

        User recipient = new User();
        recipient.setUserId(recipientId);

        ChatRoom room = new ChatRoom();
        room.setId(UUID.randomUUID());

        LocalDateTime createdAt = LocalDateTime.now().minusMinutes(5);
        LocalDateTime updatedAt = LocalDateTime.now();

        ChatMessage entity = ChatMessage.builder()
                .id(messageId)
                .chatRoom(room)
                .sender(sender)
                .recipient(recipient)
                .content("Hello world")
                .status(DeliveryStatus.DELIVERED)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        ChatMessageResponseDTO dto = mapper.toDTO(entity);

        assertThat(dto.getId()).isEqualTo(messageId);
        assertThat(dto.getSenderId()).isEqualTo(senderId);
        assertThat(dto.getRecipientId()).isEqualTo(recipientId);
        assertThat(dto.getContent()).isEqualTo("Hello world");
        assertThat(dto.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(dto.getSentAt()).isEqualTo(createdAt);
        assertThat(dto.getUpdatedAt()).isEqualTo(updatedAt);
    }
}
