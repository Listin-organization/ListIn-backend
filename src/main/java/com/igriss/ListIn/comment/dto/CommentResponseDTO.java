package com.igriss.ListIn.comment.dto;

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
public class CommentResponseDTO {
    private UUID id;
    private UUID parentCommentId;
    private UUID repliedUserId;
    private String repliedUser;
    private String content;
    private LocalDateTime createdAt;
    private String authorImagePath;
    private String authorNickName;
    private UUID authorId;
    private Integer totalReplies;
}
