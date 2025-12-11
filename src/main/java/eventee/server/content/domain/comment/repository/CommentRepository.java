package eventee.server.content.domain.comment.repository;

import eventee.server.content.domain.comment.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findCommentByCommentId(Long commentId);
}
