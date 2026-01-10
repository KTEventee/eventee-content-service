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


    private String normalizeVoteContent(String voteContent) {
        if (voteContent == null) return null;
        // 프론트가 "_" 기준으로 보내고 있음. 혹시 ","로 오면 "_"로 통일
        return voteContent.contains(",") ? voteContent.replace(",", "_") : voteContent;
    }

    private String[] parseOptions(String voteContent) {
        if (voteContent == null) return new String[0];
        // 저장은 "_"로 통일했으니 파싱도 "_" 기준
        return Arrays.stream(voteContent.split("_"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);
    }

    private Post loadPostById(long id) {
        return postRepository.findPostByPostId(id)
                .orElseThrow(() -> new BaseException(ErrorCode.POST_NOT_FOUND));
    }

    /* ======================
       게시글 생성
    ====================== */

    @Override
    @Transactional
    public Post makePost(PostRequest.PostDto request, Long memberId, String writerNickname) {

        PostType postType = PostType.from(request.type());

        String normalizedVoteContent = normalizeVoteContent(request.voteContent());
        String voteTitle = (postType == PostType.VOTE) ? request.voteTitle() : null;
        String voteContent = (postType == PostType.VOTE) ? normalizedVoteContent : null;

        Post post = Post.builder()
                .content(request.content())
                .type(postType)
                .groupId(request.groupId())      
                .eventId(request.eventId())      
                .memberId(memberId)
                .writerNickname(writerNickname)  
                .voteTitle(voteTitle)
                .voteContent(voteContent)
                .build();

        return postRepository.save(post);
    }

    /* ======================
       게시글 삭제
    ====================== */

    @Override
    @Transactional
    public void deletePost(Long id) {
        Post post = loadPostById(id);
        postRepository.delete(post);
    }

    /* ======================
       게시글 수정
    ====================== */

    @Override
    @Transactional
    public PostResponse.PostDto updatePost(PostRequest.PostDto request, Long memberId, Long postId) {
        Post post = loadPostById(postId);

        // type이 안 오면 기존 유지
        String normalizedType = (request.type() != null)
                ? request.type().trim().toUpperCase()
                : post.getPostType().name();

        // VOTE면 voteTitle/voteContent 적용, 아니면 null로 내려서 updatePost에서 제거되도록
        boolean isVote = "VOTE".equals(normalizedType);

        PostRequest.PostDto normalizedRequest = new PostRequest.PostDto(
                request.groupId() != null ? request.groupId() : post.getGroupId(),
                request.eventId() != null ? request.eventId() : post.getEventId(),
                normalizedType,
                request.content(),
                isVote ? request.voteTitle() : null,
                isVote ? normalizeVoteContent(request.voteContent()) : null
        );

        post.updatePost(normalizedRequest);
        Post saved = postRepository.save(post);

        return PostResponse.PostDto.from(saved, memberId);
    }


    /* ======================
       게시글 조회
    ====================== */

    @Override
    @Transactional(readOnly = true)
    public PostResponse.PostListByGroupDto getPostByEvent(long eventId, Long memberId) {

        List<Post> posts = postRepository.findPostsByEventId(eventId);
        return PostResponse.PostListByGroupDto.from(posts, memberId);
    }

    /* ======================
       투표
    ====================== */

    @Override
    @Transactional
    public VoteLogResponseDto vote(PostRequest.VoteDto request, Long memberId) {

        Post post = loadPostById(request.postId());

        if (post.getPostType() != PostType.VOTE) {
            throw new BaseException(ErrorCode.POST_TYPE_NOT_VOTE);
        }

        Optional<VoteLog> checkLog = voteLogRepository.findVoteLogByMemberIdAndPost(memberId, post);
        if (checkLog.isPresent()) throw new BaseException(ErrorCode.VOTE_ALREADY_DO);

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
            // 만약 ErrorCode 없으면 새로 추가하거나, 임시로 POST_NOT_FOUND 같은 걸 쓰지 말고
            // 가장 가까운 코드로 맞추세요.
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
    public void adminPost(PostRequest.AdminPostDto request, Long memberId, String writerNickname) {

        List<Long> groupIds = Arrays.stream(request.groupNums().split("_"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(Long::parseLong)
                .toList();

        PostType postType = PostType.from(request.type());

        String normalizedVoteContent = normalizeVoteContent(request.voteContent());
        String voteTitle = (postType == PostType.VOTE) ? request.voteTitle() : null;
        String voteContent = (postType == PostType.VOTE) ? normalizedVoteContent : null;

        List<Post> posts = new ArrayList<>();

        for (Long groupId : groupIds) {
            Post post = Post.builder()
                    .content(request.content())
                    .type(postType)
                    .groupId(groupId)
                    .memberId(memberId)
                    .writerNickname(writerNickname)
                    .voteTitle(voteTitle)
                    .voteContent(voteContent)
                    .build();

            posts.add(post);
        }

        postRepository.saveAll(posts);
    }
}
