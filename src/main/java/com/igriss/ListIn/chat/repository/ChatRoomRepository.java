package com.igriss.ListIn.chat.repository;

import com.igriss.ListIn.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {
    Optional<ChatRoom> findByPublication_IdAndSender_UserIdAndRecipient_UserId(UUID publicationId, UUID senderId, UUID receiverId);
    List<ChatRoom> findBySender_UserId(UUID senderUserId);

    Optional<ChatRoom> findByChatRoomId(String chatRoomId);

    void deleteChatRoomByPublicationId(UUID publicationId);
}