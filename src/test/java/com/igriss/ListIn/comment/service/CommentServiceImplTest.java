package com.igriss.ListIn.comment.service;

import com.igriss.ListIn.comment.dto.CommentRequestDTO;
import com.igriss.ListIn.comment.dto.CommentResponseDTO;
import com.igriss.ListIn.comment.entity.Comment;
import com.igriss.ListIn.comment.repository.CommentRepository;
import com.igriss.ListIn.comment.service.impl.CommentServiceImpl;
import com.igriss.ListIn.publication.entity.Publication;
import com.igriss.ListIn.publication.service.PublicationService;
import com.igriss.ListIn.user.entity.User;
import com.igriss.ListIn.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommentServiceImplTest {

    @Mock
    private PublicationService publicationService;

    @Mock
    private UserService userService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User author;
    private Publication publication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        author = User.builder().userId(UUID.randomUUID()).nickName("Davron").build();
        publication = Publication.builder().id(UUID.randomUUID()).commentsCount(0).build();
    }

    @Test
    void shouldAddCommentWithoutParentOrRepliedUser() {
        // given
        CommentRequestDTO request = new CommentRequestDTO();
        request.setContent("Nice post!");
        request.setPublicationId(publication.getId());

        when(authentication.getPrincipal()).thenReturn(author);
        when(publicationService.getByIdAsEntity(publication.getId())).thenReturn(publication);

        Comment saved = Comment.builder()
                .id(UUID.randomUUID())
                .content(request.getContent())
                .author(author)
                .publication(publication)
                .build();

        when(commentRepository.save(any(Comment.class))).thenReturn(saved);

        // when
        CommentResponseDTO response = commentService.addComment(request, authentication);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("Nice post!");
        assertThat(publication.getCommentsCount()).isEqualTo(1);

        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void shouldAddCommentWithParentAndRepliedUser() {
        // given
        UUID parentId = UUID.randomUUID();
        UUID repliedUserId = UUID.randomUUID();

        User repliedUser = User.builder().userId(repliedUserId).nickName("RepliedUser").build();
        Comment parent = Comment.builder().id(parentId).content("Parent comment").build();

        CommentRequestDTO request = new CommentRequestDTO();
        request.setContent("Reply content");
        request.setPublicationId(publication.getId());
        request.setParentId(parentId);
        request.setRepliedUserId(repliedUserId);

        when(authentication.getPrincipal()).thenReturn(author);
        when(publicationService.getByIdAsEntity(publication.getId())).thenReturn(publication);
        when(commentRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(userService.getById(repliedUserId)).thenReturn(repliedUser);

        Comment saved = Comment.builder()
                .id(UUID.randomUUID())
                .content(request.getContent())
                .author(author)
                .parent(parent)
                .repliedUser(repliedUser)
                .publication(publication)
                .build();

        when(commentRepository.save(any(Comment.class))).thenReturn(saved);

        // when
        CommentResponseDTO response = commentService.addComment(request, authentication);

        // then
        assertThat(response).isNotNull();
        assertThat(publication.getCommentsCount()).isEqualTo(1);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void shouldThrowWhenParentNotFound() {
        // given
        UUID parentId = UUID.randomUUID();
        CommentRequestDTO request = new CommentRequestDTO();
        request.setContent("Reply");
        request.setPublicationId(publication.getId());
        request.setParentId(parentId);

        when(authentication.getPrincipal()).thenReturn(author);
        when(publicationService.getByIdAsEntity(publication.getId())).thenReturn(publication);
        when(commentRepository.findById(parentId)).thenReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> commentService.addComment(request, authentication))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Parent comment not found");
    }
}
