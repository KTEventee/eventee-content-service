package eventee.server.content.domain.post.service;

import eventee.server.common.exception.BaseException;
import eventee.server.common.exception.codes.ErrorCode;
import eventee.server.content.domain.post.dto.PostRequest;
import eventee.server.content.domain.post.dto.PostResponse;
import eventee.server.content.domain.post.dto.VoteLogResponseDto;
import eventee.server.content.domain.post.model.Post;
import eventee.server.content.domain.post.model.PostType;
import eventee.server.content.domain.post.model.VoteLog;
import eventee.server.content.domain.post.repository.PostRepository;
import eventee.server.content.domain.post.repository.VoteLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final VoteLogRepository voteLogRepository;

    /* ======================
       내부 유틸
    ====================== */

    private String normalizeVoteContent(String voteContent) {
        if (voteContent == null) return null;
        return voteContent.contains(",")
                ? voteContent.replace(",", "_")
                : voteContent;
    }

    private String[] parseOptions(String voteContent) {
        if (voteContent == null) return new String[0];
        return Arrays.stream(voteContent.split("_"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);
    }

    private Post loadPostById(Long postId) {
        return postRepository.findPostByPostId(postId)
                .orElseThrow(() -> new BaseException(ErrorCode.POST_NOT_FOUND));
    }

    /* ======================
       게시글 생성
    ====================== */

    @Override
    @Transactional
    public Post makePost(PostRequest.PostDto request,
                         Long memberId) {

        PostType postType = PostType.from(request.type());

        String voteContent = (postType == PostType.VOTE)
                ? normalizeVoteContent(request.voteContent())
                : null;

        String voteTitle = (postType == PostType.VOTE)
                ? request.voteTitle()
                : null;

        Post post = Post.builder()
                .content(request.content())
                .type(postType)
                .eventId(request.eventId())
                .groupId(request.groupId())
                .memberId(memberId)
                .voteTitle(voteTitle)
                .voteContent(voteContent)
                .writerNickname(request.writerNickname())
                .build();

        return postRepository.save(post);
    }

    /* ======================
       게시글 삭제
    ====================== */

    @Override
    @Transactional
    public void deletePost(Long postId) {
        Post post = loadPostById(postId);
        postRepository.delete(post);
    }

    /* ======================
       게시글 수정
    ====================== */

    @Override
    @Transactional
    public PostResponse.PostDto updatePost(PostRequest.PostDto request,
                                           Long memberId,
                                           Long postId) {

        Post post = loadPostById(postId);

        String normalizedType = (request.type() != null)
                ? request.type().trim().toUpperCase()
                : post.getPostType().name();

        boolean isVote = "VOTE".equals(normalizedType);

        PostRequest.PostDto normalizedRequest = new PostRequest.PostDto(
                post.getGroupId(),
                null,
                normalizedType,
                request.content(),
                request.writerNickname(),
                isVote ? request.voteTitle() : null,
                isVote ? normalizeVoteContent(request.voteContent()) : null
        );

        post.updatePost(normalizedRequest);
        postRepository.save(post);

        return PostResponse.PostDto.from(post, memberId);
    }

    /* ======================
       이벤트 게시글 조회
    ====================== */

    @Override
    @Transactional(readOnly = true)
    public PostResponse.PostListByGroupDto getPostByEvent(Long eventId,
                                                          Long memberId) {

        List<Post> posts = postRepository.findPostsByEventId(eventId);
        return PostResponse.PostListByGroupDto.from(posts, memberId);
    }

    /* ======================
       투표
    ====================== */

    @Override
    @Transactional
    public VoteLogResponseDto vote(PostRequest.VoteDto request,
                                   Long memberId) {

        Post post = loadPostById(request.postId());

        if (post.getPostType() != PostType.VOTE) {
            throw new BaseException(ErrorCode.POST_TYPE_NOT_VOTE);
        }

        Optional<VoteLog> exists =
                voteLogRepository.findVoteLogByMemberIdAndPost(memberId, post);

        if (exists.isPresent()) {
            throw new BaseException(ErrorCode.VOTE_ALREADY_DO);
        }

        String[] options = parseOptions(post.getVoteContent());

        int voteNum = 0;
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(request.voteText())) {
                voteNum = i + 1;
                break;
            }
        }

        if (voteNum == 0) {
            throw new BaseException(ErrorCode.POST_TYPE_NOT_VOTE);
        }

        VoteLog log = VoteLog.builder()
                .post(post)
                .memberId(memberId)
                .voteNum(voteNum)
                .word(request.voteText())
                .build();

        post.addVoteLog(log);

        voteLogRepository.save(log);
        postRepository.save(post);

        return VoteLogResponseDto.from(post.getVoteLogs(), memberId);
    }

    /* ======================
       관리자 게시글
    ====================== */

    @Override
    @Transactional
    public void adminPost(PostRequest.AdminPostDto request,
                          Long memberId,
                          String writerNickname) {

        List<Long> groupIds = Arrays.stream(request.groupNums().split("_"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(Long::parseLong)
                .toList();

        PostType postType = PostType.from(request.type());

        String voteContent = (postType == PostType.VOTE)
                ? normalizeVoteContent(request.voteContent())
                : null;

        String voteTitle = (postType == PostType.VOTE)
                ? request.voteTitle()
                : null;

        List<Post> posts = new ArrayList<>();

        for (Long groupId : groupIds) {
            Post post = Post.builder()
                    .content(request.content())
                    .type(postType)
                    .groupId(groupId)
                    .memberId(memberId)
                    .voteTitle(voteTitle)
                    .voteContent(voteContent)
                    .build();

            post.setWriterNickname(writerNickname);
            posts.add(post);
        }

        postRepository.saveAll(posts);
    }
}
