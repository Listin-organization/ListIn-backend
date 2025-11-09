package com.igriss.ListIn.comment.service;

import com.igriss.ListIn.comment.dto.CommentRequestDTO;
import com.igriss.ListIn.comment.dto.CommentResponseDTO;
import com.igriss.ListIn.comment.entity.Comment;
import com.igriss.ListIn.comment.repository.CommentRepository;
import com.igriss.ListIn.comment.service.impl.CommentServiceImpl;
import com.igriss.ListIn.publication.dto.page.PageResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
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

    @Test
    void getCommentsForPublication_returnsPageResponse() {
        UUID publicationId = UUID.randomUUID();
        Publication publication = Publication.builder()
                .id(publicationId)
                .build();
        when(publicationService.getByIdAsEntity(publicationId)).thenReturn(publication);

        Comment comment1 = Comment.builder().id(UUID.randomUUID()).content("Comment 1").author(author).build();
        Comment comment2 = Comment.builder().id(UUID.randomUUID()).content("Comment 2").author(author).build();

        Pageable pageable = PageRequest.of(0, 2, Sort.by("createdAt").descending());
        Page<Comment> commentPage = new PageImpl<>(List.of(comment1, comment2), pageable, 2);
        when(commentRepository.findByPublicationAndParentIsNull(publication, pageable))
                .thenReturn(commentPage);

        PageResponse<CommentResponseDTO> response = commentService.getCommentsForPublication(0, 2, publicationId);

        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getNumber()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(2);
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isTrue();

        verify(commentRepository, times(1)).findByPublicationAndParentIsNull(publication, pageable);
    }

    @Test
    void getReplies_returnsPageResponse() {
        UUID parentCommentId = UUID.randomUUID();

        Comment reply1 = Comment.builder().id(UUID.randomUUID()).content("Reply 1").author(author).build();
        Comment reply2 = Comment.builder().id(UUID.randomUUID()).content("Reply 2").author(author).build();

        Pageable pageable = PageRequest.of(0, 2, Sort.by("createdAt").descending());
        Page<Comment> replyPage = new PageImpl<>(List.of(reply1, reply2), pageable, 2);
        when(commentRepository.findByParent_id(parentCommentId, pageable)).thenReturn(replyPage);

        PageResponse<CommentResponseDTO> response = commentService.getReplies(0, 2, parentCommentId);

        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getNumber()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(2);
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isTrue();

        verify(commentRepository, times(1)).findByParent_id(parentCommentId, pageable);
    }

}
