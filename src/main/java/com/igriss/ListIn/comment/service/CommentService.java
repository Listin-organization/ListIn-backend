package com.igriss.ListIn.comment.service;

import com.igriss.ListIn.comment.dto.CommentRequestDTO;
import com.igriss.ListIn.comment.dto.CommentResponseDTO;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface CommentService {
    CommentResponseDTO addComment(CommentRequestDTO request, Authentication currentUser);
    List<CommentResponseDTO> getCommentsForPublication(UUID publicationId);

    List<CommentResponseDTO> getReplies(UUID parentCommentId);
}
