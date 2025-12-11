package eventee.server.content.domain.comment.service;

import eventee.server.content.domain.comment.dto.CommentRequest;
import eventee.server.content.domain.member.model.Member;

public interface CommentService {
    void makeComment(CommentRequest.CommentDto request, Member member);
    void deleteComment(long commentId);
    void updateComment(CommentRequest.CommentUpdateDto request);
}
