package com.igriss.ListIn.comment.mapper;

import com.igriss.ListIn.comment.dto.CommentResponseDTO;
import com.igriss.ListIn.comment.entity.Comment;

import java.util.List;

public class CommentMapper {

    public static CommentResponseDTO toDto(Comment comment) {
        return CommentResponseDTO
                .builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .authorNickName(comment.getAuthor().getNickName())
                .authorId(comment.getAuthor().getUserId())
                .replies(comment.getReplies() != null ? comment.getReplies().stream().map(CommentMapper::toDto).toList() : List.of())
                .build();
    }
}
