package com.igriss.ListIn.comment.controller;

import com.igriss.ListIn.comment.dto.CommentRequestDTO;
import com.igriss.ListIn.comment.dto.CommentResponseDTO;
import com.igriss.ListIn.comment.service.CommentService;
import com.igriss.ListIn.publication.dto.page.PageResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @Mock
    private Authentication authentication;

    private CommentController controller;

    private CommentResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new CommentController(commentService);

        responseDTO = new CommentResponseDTO();
        responseDTO.setId(UUID.randomUUID());
        responseDTO.setContent("Test content");
    }

    @Test
    void addComment_shouldReturnResponseEntity() {
        CommentRequestDTO request = new CommentRequestDTO();
        request.setContent("Some comment");
        request.setPublicationId(UUID.randomUUID());

        when(commentService.addComment(eq(request), eq(authentication))).thenReturn(responseDTO);

        ResponseEntity<CommentResponseDTO> response = controller.addComment(request, authentication);

        assertThat(response).isNotNull();
        assertThat(response.getBody()).isEqualTo(responseDTO);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().getContent()).isEqualTo("Test content");
        verify(commentService).addComment(request, authentication);
    }

    @Test
    void getCommentsForPublication_shouldReturnResponseEntity() {
        UUID publicationId = UUID.randomUUID();
        PageResponse<CommentResponseDTO> pageResponse = new PageResponse<>(
                List.of(responseDTO), 0, 10, 1, 1, true, true
        );

        when(commentService.getCommentsForPublication(0, 10, publicationId)).thenReturn(pageResponse);

        ResponseEntity<PageResponse<CommentResponseDTO>> response =
                controller.getCommentsForPublication(0, 10, publicationId);

        assertThat(response).isNotNull();
        assertThat(response.getBody()).isEqualTo(pageResponse);
        verify(commentService).getCommentsForPublication(0, 10, publicationId);
    }

    @Test
    void getReplies_shouldReturnResponseEntity() {
        UUID parentCommentId = UUID.randomUUID();
        PageResponse<CommentResponseDTO> pageResponse = new PageResponse<>(
                List.of(responseDTO), 0, 10, 1, 1, true, true
        );

        when(commentService.getReplies(0, 10, parentCommentId)).thenReturn(pageResponse);

        ResponseEntity<PageResponse<CommentResponseDTO>> response =
                controller.getReplies(0, 10, parentCommentId);

        assertThat(response).isNotNull();
        assertThat(response.getBody()).isEqualTo(pageResponse);
        verify(commentService).getReplies(0, 10, parentCommentId);
    }
}
