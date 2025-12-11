package eventee.server.content.domain.comment.dto;

import eventee.server.content.domain.client.member.MemberListDto;
import eventee.server.content.domain.comment.model.Comment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public class CommentResponse {
    public record CommentDto(
            @Schema(description = "댓글 ID", example = "7")
            long commentId,
            @Schema(description = "땟글 작성자 ID", example = "")
            Long writerId,
            @Schema(description = "댓글 내용", example = "좋은 글 잘 읽었습니다!")
            String content,
            @Schema(description = "현재 로그인한 사용자가 작성한 댓글인지 여부", example = "true")
            boolean isWrite,
            @Schema(description = "댓글 작성 일시", example = "2025-11-24T10:15:30")
            LocalDateTime createdAt
    ){
        public static CommentDto from(Comment c, Long memberId){
            return new CommentDto(
                    c.getCommentId(),
                    memberId,
                    c.getContent(),
                    c.getWriterId().equals(memberId),
                    c.getCreatedAt()
            );
        }

        public static List<CommentDto> from(List<Comment> comments, Long memberId){
            return comments.stream().map(
                    comment -> from(comment,memberId)
            ).toList();
        }
    }
}
