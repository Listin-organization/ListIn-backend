package com.igriss.ListIn.comment.service.impl;

import com.igriss.ListIn.comment.dto.CommentRequestDTO;
import com.igriss.ListIn.comment.dto.CommentResponseDTO;
import com.igriss.ListIn.comment.entity.Comment;
import com.igriss.ListIn.comment.mapper.CommentMapper;
import com.igriss.ListIn.comment.repository.CommentRepository;
import com.igriss.ListIn.comment.service.CommentService;
import com.igriss.ListIn.publication.dto.page.PageResponse;
import com.igriss.ListIn.publication.entity.Publication;
import com.igriss.ListIn.publication.service.PublicationService;
import com.igriss.ListIn.user.entity.User;
import com.igriss.ListIn.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final PublicationService publicationService;
    private final UserService userService;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public CommentResponseDTO addComment(CommentRequestDTO request, Authentication currentUser) {
        User author = (User) currentUser.getPrincipal();

        Publication publication = publicationService.getById(request.getPublicationId());

        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent comment not found"));
        }

        publication.setCommentsCount(publication.getCommentsCount() + 1);

        Comment comment = Comment.builder()
                .content(request.getContent())
                .repliedUser(request.getRepliedUserId() != null ? userService.getById(request.getRepliedUserId()) : null)
                .publication(publication)
                .author(author)
                .parent(parent)
                .build();

        Comment saved = commentRepository.save(comment);
        return CommentMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommentResponseDTO> getCommentsForPublication(int page, int size, UUID publicationId) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Publication publication = publicationService.getById(publicationId);
        Page<Comment> topLevelComments = commentRepository.findByPublicationAndParentIsNull(publication, pageable);

        List<CommentResponseDTO> dtoComments = topLevelComments.stream()
                .map(CommentMapper::toDto)
                .toList();

        return getPageResponse(dtoComments, topLevelComments);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommentResponseDTO> getReplies(int page, int size, UUID parentCommentId) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Comment> repliesPage = commentRepository.findByParent_id(parentCommentId, pageable);
        List<CommentResponseDTO> dtoReplies = repliesPage.stream()
                .map(CommentMapper::toDto).toList();

        return getPageResponse(dtoReplies, repliesPage);
    }

    @NotNull
    private static PageResponse<CommentResponseDTO> getPageResponse(List<CommentResponseDTO> dtoComments, Page<Comment> topLevelComments) {
        return new PageResponse<>(
                dtoComments,
                topLevelComments.getNumber(),
                topLevelComments.getSize(),
                topLevelComments.getTotalElements(),
                topLevelComments.getTotalPages(),
                topLevelComments.isFirst(),
                topLevelComments.isLast()
        );
    }
}
