package eventee.server.content.domain.post.repository;

import eventee.server.content.domain.group.model.Group;
import eventee.server.content.domain.post.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post,Long> {
    Optional<Post> findPostByPostId(Long id);

    List<Post> findAllByGroupAndIsDeletedFalse(Group group);
}
