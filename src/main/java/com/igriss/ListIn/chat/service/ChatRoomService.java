package com.igriss.ListIn.chat.service;

import com.igriss.ListIn.chat.entity.ChatRoom;
import com.igriss.ListIn.chat.repository.ChatRoomRepository;
import com.igriss.ListIn.publication.entity.Publication;
import com.igriss.ListIn.publication.service.PublicationService;
import com.igriss.ListIn.user.entity.User;
import com.igriss.ListIn.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserService userService;

    public Optional<ChatRoom> getChatRoom(UUID senderId, UUID recipientId, boolean createNewRoomIfNotExists) {
        return chatRoomRepository.findBySender_UserIdAndRecipient_UserId(senderId, recipientId)
                .or(() -> {
                    if (createNewRoomIfNotExists) {
                        ChatRoom chatRoom = createRoom(senderId, recipientId);
                        return Optional.of(chatRoom);
                    }
                    return Optional.empty();
                });
    }

    private ChatRoom createRoom(UUID senderId, UUID recipientId) {

        String chatId = String.format("%s_%s", senderId, recipientId);

        User sender = userService.getById(senderId);
        User recipient = userService.getById(recipientId);

        ChatRoom senderRecipient = ChatRoom.builder()
                .chatRoomId(chatId)
                .sender(sender)
                .recipient(recipient)
                .unreadMessagesCount(0L)
                .build();

        ChatRoom recipientSender = ChatRoom.builder()
                .chatRoomId(chatId)
                .sender(recipient)
                .recipient(sender)
                .unreadMessagesCount(0L)
                .build();

        chatRoomRepository.save(senderRecipient);
        chatRoomRepository.save(recipientSender);

        return senderRecipient;
    }

    public Optional<ChatRoom> getChatRoomById(UUID chatRoomId) {
        return chatRoomRepository.findById(chatRoomId);
    }

    @Transactional
    public void incrementUnreadCount(ChatRoom chatRoom) {
        chatRoom.setUnreadMessagesCount(chatRoom.getUnreadMessagesCount() + 1);
        chatRoomRepository.save(chatRoom);
    }

    @Transactional
    public void decrementUnreadCount(ChatRoom chatRoom, long count) {
        long updatedCount = Math.max(0, chatRoom.getUnreadMessagesCount() - count);
        chatRoom.setUnreadMessagesCount(updatedCount);
        chatRoomRepository.save(chatRoom);
    }

}
