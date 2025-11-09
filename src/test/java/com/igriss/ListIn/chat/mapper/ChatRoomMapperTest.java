package com.igriss.ListIn.chat.mapper;

import com.igriss.ListIn.chat.dto.ChatRoomResponseDTO;
import com.igriss.ListIn.chat.entity.ChatRoom;
import com.igriss.ListIn.user.entity.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ChatRoomMapperTest {

    private final ChatRoomMapper mapper = new ChatRoomMapper();

    @Test
    void toDTO_mapsAllFieldsCorrectly() {
        UUID recipientId = UUID.randomUUID();

        User recipient = new User();
        recipient.setUserId(recipientId);
        recipient.setNickName("JohnDoe");
        recipient.setProfileImagePath("/images/profile.jpg");

        ChatRoom chatRoom = ChatRoom.builder()
                .chatRoomId("pub123_sender_rec")
                .recipient(recipient)
                .unreadMessagesCount(5L)
                .build();

        ChatRoomResponseDTO dto = mapper.toDTO(chatRoom);

        assertThat(dto.getChatRoomId()).isEqualTo("pub123_sender_rec");
        assertThat(dto.getRecipientId()).isEqualTo(recipientId);
        assertThat(dto.getRecipientNickname()).isEqualTo("JohnDoe");
        assertThat(dto.getRecipientImagePath()).isEqualTo("/images/profile.jpg");
        assertThat(dto.getUnreadMessages()).isEqualTo(5L);

        assertThat(dto.getLastMessage()).isNull();
    }
}
