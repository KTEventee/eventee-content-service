package eventee.server.content.domain.comment.service;

import eventee.server.content.domain.comment.dto.CommentRequest;

public interface CommentService {
    void makeComment(CommentRequest.CommentDto request, Long memberId);
    void deleteComment(long commentId);
    void updateComment(CommentRequest.CommentUpdateDto request);
}
