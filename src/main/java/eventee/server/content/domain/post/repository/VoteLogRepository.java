package eventee.server.content.domain.post.repository;

import eventee.server.content.domain.post.model.Post;
import eventee.server.content.domain.post.model.VoteLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoteLogRepository extends JpaRepository<VoteLog,Long> {
    Optional<VoteLog> findVoteLogByMemberIdAndPost(Long memberId, Post post);
}
