package eventee.server.content.domain.post.controller;

import eventee.server.common.jwt.exception.JwtErrorCode;
import eventee.server.common.jwt.exception.JwtHandler;
import eventee.server.content.domain.post.dto.PostRequest;
import eventee.server.content.domain.post.dto.PostResponse;
import eventee.server.content.domain.post.dto.VoteLogResponseDto;
import eventee.server.content.domain.post.model.Post;
import eventee.server.content.domain.post.service.PostService;
import eventee.server.common.exception.BaseResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
@Slf4j
@Tag(name = "Post", description = "게시글 / 투표 API")
public class PostController {

    private final PostService postService;

    /* ======================
       게시글 생성
    ====================== */
    @PostMapping
    public BaseResponse<PostResponse.PostDto> createPost(
            HttpServletRequest request,
            @RequestBody PostRequest.PostDto requestDto
    ) {
        Long memberId = (Long) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        if (memberId == null || nickname == null) {
            throw new JwtHandler(JwtErrorCode.JWT_MISSING_TOKEN);
        }

        Post post = postService.makePost(requestDto, memberId, nickname);
        return BaseResponse.onSuccess(PostResponse.PostDto.from(post, memberId));
    }

    /* ======================
       게시글 삭제
    ====================== */
    @DeleteMapping("/{postId}")
    public BaseResponse<String> deletePost(
            @PathVariable long postId
    ) {
        postService.deletePost(postId);
        return BaseResponse.onSuccess("success");
    }

    /* ======================
       게시글 수정
    ====================== */
    @PatchMapping("/{postId}")
    public BaseResponse<PostResponse.PostDto> updatePost(
            HttpServletRequest request,
            @PathVariable Long postId,
            @RequestBody PostRequest.PostDto requestDto
    ) {
        Long memberId = (Long) request.getAttribute("memberId");

        if (memberId == null) {
            throw new JwtHandler(JwtErrorCode.JWT_MISSING_TOKEN);
        }

        return BaseResponse.onSuccess(
                postService.updatePost(requestDto, memberId, postId)
        );
    }

    /* ======================
       이벤트 전체 게시글 조회
    ====================== */
    @GetMapping("/{eventId}")
    public BaseResponse<PostResponse.PostListByGroupDto> getPostByEvent(
            HttpServletRequest request,
            @PathVariable Long eventId
    ) {
        Long memberId = (Long) request.getAttribute("memberId");

        if (memberId == null) {
            throw new JwtHandler(JwtErrorCode.JWT_MISSING_TOKEN);
        }

        return BaseResponse.onSuccess(
                postService.getPostByEvent(eventId, memberId)
        );
    }

    /* ======================
       투표
    ====================== */
    @PostMapping("/vote")
    public BaseResponse<VoteLogResponseDto> vote(
            HttpServletRequest request,
            @RequestBody PostRequest.VoteDto requestDto
    ) {
        Long memberId = (Long) request.getAttribute("memberId");

        if (memberId == null) {
            throw new JwtHandler(JwtErrorCode.JWT_MISSING_TOKEN);
        }

        return BaseResponse.onSuccess(
                postService.vote(requestDto, memberId)
        );
    }

    /* ======================
       관리자 게시글
    ====================== */
    @PostMapping("/admin")
    public BaseResponse<String> adminPost(
            HttpServletRequest request,
            @RequestBody PostRequest.AdminPostDto requestDto
    ) {
        Long memberId = (Long) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        if (memberId == null || nickname == null) {
            throw new JwtHandler(JwtErrorCode.JWT_MISSING_TOKEN);
        }

        postService.adminPost(requestDto, memberId, nickname);
        return BaseResponse.onSuccess("success");
    }
}
