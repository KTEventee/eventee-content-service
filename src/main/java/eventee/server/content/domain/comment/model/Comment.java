package eventee.server.content.domain.comment.model;

import eventee.server.content.domain.comment.dto.CommentRequest;
import eventee.server.content.domain.post.model.Post;
import eventee.server.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "comment")
@SQLDelete(sql = "UPDATE comment SET is_deleted = true, deleted_at = now() where comment_id = ?")
@SQLRestriction("is_deleted is FALSE")
public class Comment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;
    private Long writerId;
    private String writerNickname;
    private String content;

    @ManyToOne
    private Post post;

    @Builder
    public Comment(String content, Post post, Long writerId){
        this.content = content;
        this.post = post;
        this.writerId = writerId;
    }

    public void updateComment(CommentRequest.CommentUpdateDto dto){
        this.content = dto.content();
    }
}
