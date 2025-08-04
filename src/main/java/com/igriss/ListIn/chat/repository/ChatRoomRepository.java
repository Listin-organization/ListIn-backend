package com.igriss.ListIn.chat.repository;

import com.igriss.ListIn.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {
    Optional<ChatRoom> findBySender_UserIdAndRecipient_UserId(UUID senderId, UUID recipientId);

    List<ChatRoom> findBySender_UserId(UUID senderUserId);

}