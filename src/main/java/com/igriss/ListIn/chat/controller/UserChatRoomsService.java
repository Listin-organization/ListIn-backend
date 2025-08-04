package com.igriss.ListIn.chat.controller;

import com.igriss.ListIn.chat.dto.ChatRoomResponseDTO;
import com.igriss.ListIn.chat.entity.ChatRoom;
import com.igriss.ListIn.chat.mapper.ChatMessageMapper;
import com.igriss.ListIn.chat.mapper.ChatRoomMapper;
import com.igriss.ListIn.chat.repository.ChatRoomRepository;
import com.igriss.ListIn.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserChatRoomsService {

    private final ChatMessageMapper messageMapper;
    private final ChatMessageService chatMessageService;
    private final ChatRoomMapper chatRoomMapper;
    private final ChatRoomRepository chatRoomRepository;

    public List<ChatRoomResponseDTO> getUserChatRooms(UUID userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findBySender_UserId(userId);

        return chatRooms.stream()
                .map(chatRoomMapper::toDTO)
                .peek(chatRoomDTO -> {
                    // Set last message
                    chatMessageService.findLastMessage(chatRoomDTO.getChatRoomId())
                            .map(messageMapper::toDTO)
                            .ifPresent(chatRoomDTO::setLastMessage);
                })
                .toList();
    }
}
