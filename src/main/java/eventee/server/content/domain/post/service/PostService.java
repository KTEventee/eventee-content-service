package eventee.server.content.domain.post.service;

import eventee.server.content.domain.member.model.Member;
import eventee.server.content.domain.post.dto.PostRequest;
import eventee.server.content.domain.post.dto.PostRequest.PostDto;
import eventee.server.content.domain.post.dto.PostResponse;
import eventee.server.content.domain.post.dto.VoteLogResponseDto;
import eventee.server.content.domain.post.model.Post;

public interface PostService {

    Post makePost(PostDto request, Member member);
    void deletePost(long id);

    PostResponse.PostDto
    updatePost(PostRequest.PostDto request,Member member,Long postId);
    PostResponse.PostListByGroupDto getPostByEvent(long eventId,Member member);
    VoteLogResponseDto vote(PostRequest.VoteDto request, Member member);

    void adminPost(PostRequest.AdminPostDto request,Member member);
}
