package com.igriss.ListIn.comment.repository;

import com.igriss.ListIn.comment.entity.Comment;
import com.igriss.ListIn.publication.entity.Publication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

  List<Comment> findByPublicationAndParentIsNullOrderByCreatedAtDesc(Publication publication);
  List<Comment> findByParent_id(UUID parentId);

}