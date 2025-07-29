package com.igriss.ListIn.comment.service;

import com.igriss.ListIn.comment.dto.CommentRequestDTO;
import com.igriss.ListIn.comment.dto.CommentResponseDTO;
import com.igriss.ListIn.publication.dto.page.PageResponse;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface CommentService {
    CommentResponseDTO addComment(CommentRequestDTO request, Authentication currentUser);
    PageResponse<CommentResponseDTO> getCommentsForPublication(int page, int size, UUID publicationId);

    PageResponse<CommentResponseDTO> getReplies(int page, int size, UUID parentCommentId);
}
