package eventee.server.content.domain.post.model;

import eventee.server.content.domain.comment.model.Comment;
import eventee.server.content.domain.post.dto.PostRequest;
import eventee.server.content.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "post")
@SQLDelete(sql = "UPDATE post SET is_deleted = true, deleted_at = now() WHERE post_id = ?")
@SQLRestriction("is_deleted is FALSE")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    private Long memberId;

    private String content;
    private String voteTitle = null;
    private String voteContent = null;

    @Enumerated(EnumType.STRING)
    private PostType postType;
    private Long groupId;
    private Long eventId;

    @OneToMany(mappedBy = "post")
    private List<VoteLog> voteLogs = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<Comment> comments = new ArrayList<>();

    public void addComment(Comment c){
        if(comments.contains(c)) return;
        comments.add(c);
    }

    public void deleteComment(Comment c){
        if(!comments.contains(c)) return;
        comments.remove(c);
    }

    public void addVoteLog(VoteLog log){
        if(voteLogs.contains(log)) return;
        voteLogs.add(log);
    }

    public void deleteLog(VoteLog log){
        if(!voteLogs.contains(log)) return;
        voteLogs.remove(log);
    }

    @Builder
    public Post(String content, PostType type, Long groupId, String voteTitle, String voteContent,Long eventId, Long memberId){
        this.content = content;
        this.postType = type;
        this.memberId = memberId;
        this.eventId = eventId;
        this.groupId = groupId;
        if(this.postType.equals(PostType.VOTE)) {
            this.voteTitle = voteTitle;
            this.voteContent = voteContent;
        }
    }

    public void updatePost(PostRequest.PostDto dto) {
        // content는 프론트가 빈 문자열 보내면 빈 문자열이 맞음 (유지 X)
        if (dto.content() != null) {
            this.content = dto.content();
        }

        // type 변경 요청이 있을 때만 변경
        if (dto.type() != null) {
            this.postType = PostType.from(dto.type());
        }

        // 투표 게시글일 경우
        if (this.postType == PostType.VOTE) {

            // title이 null이면 기존 유지
            if (dto.voteTitle() != null) {
                this.voteTitle = dto.voteTitle();
            }

            // content가 null이면 기존 유지
            if (dto.voteContent() != null) {
                this.voteContent = dto.voteContent();
            }

        } else {
            // 일반 게시글로 변경될 때만 vote 필드 제거
            this.voteTitle = null;
            this.voteContent = null;
        }
    }




}
