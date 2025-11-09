package com.igriss.ListIn.comment.controller;

import com.igriss.ListIn.comment.dto.CommentRequestDTO;
import com.igriss.ListIn.comment.dto.CommentResponseDTO;
import com.igriss.ListIn.comment.service.CommentService;
import com.igriss.ListIn.publication.dto.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService service;

    @PostMapping
    public ResponseEntity<CommentResponseDTO> addComment(@RequestBody CommentRequestDTO request, Authentication currentUser) {
        return ResponseEntity.ok(service.addComment(request, currentUser));
    }

    @GetMapping("/publication/{publicationId}")
    public ResponseEntity<PageResponse<CommentResponseDTO>> getCommentsForPublication(@RequestParam(name = "page", defaultValue = "0", required = false) int page,
                                                                                      @RequestParam(name = "size", defaultValue = "10", required = false) int size,
                                                                                      @PathVariable UUID publicationId) {
        return ResponseEntity.ok(service.getCommentsForPublication(page, size, publicationId));
    }

    @GetMapping("/parent/{parentCommentId}/replies")
    public ResponseEntity<PageResponse<CommentResponseDTO>> getReplies(@RequestParam(name = "page", defaultValue = "0", required = false) int page,
                                                               @RequestParam(name = "size", defaultValue = "10", required = false) int size,
                                                               @PathVariable UUID parentCommentId) {
        return ResponseEntity.ok(service.getReplies(page, size, parentCommentId));
    }
}
