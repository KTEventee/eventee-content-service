package eventee.server.content.domain.post.dto;

import eventee.server.content.domain.comment.dto.CommentResponse;
import eventee.server.content.domain.post.model.Post;
import eventee.server.content.domain.post.model.PostType;
import eventee.server.content.domain.post.model.VoteLog;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PostResponse {

    @Schema(description = "투표 옵션 DTO")
    public record VoteOptionDto(
        @Schema(description = "옵션 번호", example = "1") int optionNo,
        @Schema(description = "옵션 텍스트", example = "짜장면") String text,
        @Schema(description = "득표 수", example = "12") int votes,
        @Schema(description = "득표 비율 (%)", example = "60") int percent,
        @Schema(description = "내가 선택한 옵션 여부", example = "true") boolean isMine
    ) {}

    @Schema(description = "게시글 DTO")
    public record PostDto(
        @Schema(description = "게시글 ID", example = "10") long postId,
        @Schema(description = "내용") String content,
        @Schema(description = "작성자 ID") Long writerId,
        @Schema(description = "게시글 타입 (text, vote)") String type,
        @Schema(description = "투표 질문") String voteTitle,
        @Schema(description = "투표 옵션 리스트") List<VoteOptionDto> voteOptions,
        @Schema(description = "댓글 리스트") List<CommentResponse.CommentDto> comments,
        @Schema(description = "내가 작성한 글인지 여부") boolean isWrite
    ) {

        public static PostDto from(Post post, Long memberId) {

            List<CommentResponse.CommentDto> comments =
                CommentResponse.CommentDto.from(post.getComments(), memberId);

            List<VoteOptionDto> voteOptionDtos = new ArrayList<>();

            if (post.getPostType() == PostType.VOTE) {

                // 안정적인 옵션 파싱 (공백/쉼표 여러 형태 모두 처리)
                String[] options = post.getVoteContent() != null
                    ? post.getVoteContent().split("\\s*,\\s*")
                    : new String[0];

                List<VoteLog> logs = post.getVoteLogs();
                int totalVotes = logs.size();

                for (int i = 0; i < options.length; i++) {
                    int optionNo = i + 1;
                    String text = options[i];

                    int votes = (int) logs.stream()
                        .filter(v -> v.getVoteNum() == optionNo)
                        .count();

                    int percent = totalVotes > 0
                        ? (int) Math.round((votes * 100.0) / totalVotes)
                        : 0;

                    boolean isMine = logs.stream()
                        .anyMatch(v ->
                            v.getMemberId().equals(memberId) &&
                                v.getVoteNum() == optionNo
                        );

                    voteOptionDtos.add(
                        new VoteOptionDto(optionNo, text, votes, percent, isMine)
                    );
                }
            }


            return new PostDto(
                post.getPostId(),
                post.getContent(),
                post.getMemberId(),
                post.getPostType().type.toLowerCase(),
                post.getVoteTitle(),
                voteOptionDtos,
                comments,
                    post.getMemberId().equals(memberId)
            );
        }


        public static List<PostDto> from(List<Post> posts, Long memberId) {
            return posts.stream()
                .map(post -> PostDto.from(post, memberId))
                .collect(Collectors.toList());
        }
    }

    @Schema(description = "그룹별 게시글 리스트 DTO")
    public record PostListDto(
            @Schema(description = "그룹 번호", example = "1") Long groupNum,
            @Schema(description = "게시글 리스트") List<PostDto> posts
    ) {
        public static PostListDto of(Long groupNum, List<Post> posts, Long memberId) {
            return new PostListDto(
                    groupNum,
                    posts.stream()
                            .map(post -> PostDto.from(post, memberId))
                            .toList()
            );
        }
    }

    @Schema(description = "이벤트 내 전체 그룹 게시글 리스트 DTO")
    public record PostListByGroupDto(
            @Schema(description = "그룹 게시글 목록 리스트") List<PostListDto> lists
    ) {
        public static PostListByGroupDto from(List<Post> posts, Long memberId) {
            // groupId 기준으로 묶기
            Map<Long, List<Post>> grouped = posts.stream()
                    .collect(Collectors.groupingBy(Post::getGroupId));

            List<PostListDto> listDtos = grouped.entrySet().stream()
                    .map(entry -> PostListDto.of(
                            entry.getKey(),
                            entry.getValue(),
                            memberId
                    ))
                    .sorted(Comparator.comparing(PostListDto::groupNum))
                    .toList();

            return new PostListByGroupDto(listDtos);
        }
    }
}
