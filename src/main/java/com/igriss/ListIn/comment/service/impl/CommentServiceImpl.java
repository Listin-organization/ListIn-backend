package com.igriss.ListIn.comment.service.impl;

import com.igriss.ListIn.comment.dto.CommentRequestDTO;
import com.igriss.ListIn.comment.dto.CommentResponseDTO;
import com.igriss.ListIn.comment.entity.Comment;
import com.igriss.ListIn.comment.mapper.CommentMapper;
import com.igriss.ListIn.comment.repository.CommentRepository;
import com.igriss.ListIn.comment.service.CommentService;
import com.igriss.ListIn.publication.entity.Publication;
import com.igriss.ListIn.publication.service.PublicationService;
import com.igriss.ListIn.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final PublicationService publicationService;

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

        Comment comment = Comment.builder()
                .content(request.getContent())
                .publication(publication)
                .author(author)
                .parent(parent)
                .build();

        Comment saved = commentRepository.save(comment);
        return CommentMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentsForPublication(UUID publicationId) {

        Publication publication = publicationService.getById(publicationId);
        List<Comment> topLevelComments = commentRepository.findByPublicationAndParentIsNullOrderByCreatedAtDesc(publication);

        return topLevelComments.stream()
                .map(CommentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getReplies(UUID parentCommentId) {
        return commentRepository.findByParent_id(parentCommentId).stream()
                .map(CommentMapper::toDto).toList();
    }
}
