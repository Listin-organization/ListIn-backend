package com.igriss.ListIn.comment.mapper;

import com.igriss.ListIn.comment.dto.CommentResponseDTO;
import com.igriss.ListIn.comment.entity.Comment;

public class CommentMapper {

    public static CommentResponseDTO toDto(Comment comment) {
        return CommentResponseDTO
                .builder()
                .id(comment.getId())
                .repliedUser(comment.getRepliedUser())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .authorImagePath(comment.getAuthor().getProfileImagePath())
                .authorNickName(comment.getAuthor().getNickName())
                .authorId(comment.getAuthor().getUserId())
                .totalReplies(comment.getReplies() != null ? comment.getReplies().size() : 0)
                .build();
    }
}
