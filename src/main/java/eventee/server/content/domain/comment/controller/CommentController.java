package eventee.server.content.domain.comment.controller;

import eventee.server.common.jwt.exception.JwtErrorCode;
import eventee.server.common.jwt.exception.JwtHandler;
import eventee.server.content.domain.comment.dto.CommentRequest;
import eventee.server.content.domain.comment.service.CommentService;
import eventee.server.common.exception.BaseException;
import eventee.server.common.exception.BaseResponse;
import eventee.server.common.exception.codes.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
@Tag(name = "Comment", description = "댓글 API")
@Slf4j
public class CommentController {

    private final CommentService commentService;

    @Operation(
        summary = "댓글 수정",
        description = "기존 댓글 내용을 수정합니다. 작성자 본인만 수정할 수 있습니다."
    )
    @PatchMapping
    public BaseResponse<String> updateComment(
            HttpServletRequest request, Authentication authentication,
        @RequestBody CommentRequest.CommentUpdateDto requestDto){
        Long memberId = (Long) authentication.getPrincipal();
        if (memberId == null) {
            throw new JwtHandler(JwtErrorCode.JWT_MISSING_TOKEN);
        }
        commentService.updateComment(requestDto);
        return BaseResponse.onSuccess("success");
    }

    @Operation(
        summary = "댓글 생성",
        description = "새로운 댓글을 작성합니다. 로그인한 사용자 정보는 @CurrentMember에서 주입됩니다."
    )
    @PostMapping
    public BaseResponse<String> makeComment(
        HttpServletRequest request,
        Authentication authentication,
        @RequestBody CommentRequest.CommentDto requestDto){
        Long memberId = (Long) authentication.getPrincipal();
        if (memberId == null) {
            throw new JwtHandler(JwtErrorCode.JWT_MISSING_TOKEN);
        }
        commentService.makeComment(requestDto, memberId);
        return BaseResponse.onSuccess("success");
    }

    @Operation(
        summary = "댓글 삭제",
        description = "특정 댓글을 삭제합니다. 작성자 또는 관리자만 삭제 가능합니다."
    )
    @DeleteMapping("/{commentId}")
    public BaseResponse<String> deleteComment(
        HttpServletRequest request,
        Authentication authentication,
        @PathVariable long commentId){
        Long memberId = (Long) authentication.getPrincipal();
        if (memberId == null) {
            throw new JwtHandler(JwtErrorCode.JWT_MISSING_TOKEN);
        }
        commentService.deleteComment(commentId);
        return BaseResponse.onSuccess("success");
    }
}
