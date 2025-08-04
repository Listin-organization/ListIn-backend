package com.igriss.ListIn.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomResponseDTO {
    private String chatRoomId;

    private UUID recipientId;
    private String recipientImagePath;
    private String recipientNickname;

    private Long unreadMessages;
    private ChatMessageResponseDTO lastMessage;
}
