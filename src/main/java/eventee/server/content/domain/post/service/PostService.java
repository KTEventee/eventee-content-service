package eventee.server.content.domain.post.service;

import eventee.server.content.domain.post.dto.PostRequest;
import eventee.server.content.domain.post.dto.PostResponse;
import eventee.server.content.domain.post.dto.VoteLogResponseDto;
import eventee.server.content.domain.post.model.Post;


public interface PostService {


    /* ======================
       게시글 생성
       ====================== */
    Post makePost(
            PostRequest.PostDto request,
            Long memberId,
            String writerNickname
    );

    /* ======================
       게시글 삭제
       ====================== */
    void deletePost(long postId);

    /* ======================
       게시글 수정
       ====================== */
    PostResponse.PostDto updatePost(
            PostRequest.PostDto request,
            Long memberId,
            Long postId
    );

    /* ======================
       이벤트 내 게시글 조회
       ====================== */
    PostResponse.PostListByGroupDto getPostByEvent(
            long eventId,
            Long memberId
    );


    /* ======================
       투표
       ====================== */
    VoteLogResponseDto vote(
            PostRequest.VoteDto request,
            Long memberId
    );

    /* ======================
       관리자 게시글 등록
       ====================== */
    void adminPost(
            PostRequest.AdminPostDto request,
            Long memberId,
            String writerNickname
    );
}

