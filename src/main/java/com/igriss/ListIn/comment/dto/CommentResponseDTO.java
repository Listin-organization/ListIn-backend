package com.igriss.ListIn.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponseDTO {
    private UUID id;
    private String content;
    private LocalDateTime createdAt;
    private String authorNickName;
    private UUID authorId;
    private List<CommentResponseDTO> replies;
}
