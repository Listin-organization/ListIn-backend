package com.igriss.ListIn.chat.dto;

import com.igriss.ListIn.chat.enums.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResponseDTO {
    private UUID id;
    private UUID senderId;
    private UUID recipientId;
    private String content;
    private DeliveryStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime updatedAt;
}
