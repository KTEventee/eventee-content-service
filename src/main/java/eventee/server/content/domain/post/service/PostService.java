package eventee.server.content.domain.post.service;

import eventee.server.content.domain.post.dto.PostRequest;
import eventee.server.content.domain.post.dto.PostResponse;
import eventee.server.content.domain.post.dto.VoteLogResponseDto;
import eventee.server.content.domain.post.model.Post;


public interface PostService {

    Post makePost(PostRequest.PostDto request,
                  Long memberId);

    void deletePost(Long postId);

    PostResponse.PostDto updatePost(PostRequest.PostDto request,
                                    Long memberId,
                                    Long postId);

    PostResponse.PostListByGroupDto getPostByEvent(Long eventId,
                                                   Long memberId);

    VoteLogResponseDto vote(PostRequest.VoteDto request,
                            Long memberId);

    void adminPost(PostRequest.AdminPostDto request,
                   Long memberId,
                   String writerNickname);
}
