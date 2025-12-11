package eventee.server.content.domain.post.service;

import eventee.server.content.domain.post.dto.PostRequest;
import eventee.server.content.domain.post.dto.PostResponse;
import eventee.server.content.domain.post.dto.VoteLogResponseDto;
import eventee.server.content.domain.post.model.Post;
import eventee.server.content.domain.post.model.PostType;
import eventee.server.content.domain.post.model.VoteLog;
import eventee.server.content.domain.post.repository.PostRepository;
import eventee.server.content.domain.post.repository.VoteLogRepository;
import eventee.server.content.global.exception.BaseException;
import eventee.server.content.global.exception.codes.ErrorCode;
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

    private String normalizeContent(String content) {
        if (content == null) return null;
        if (content.contains(",")) return content.replace(",", "_");
        return content;
    }

    private String[] parseOptions(String voteContent) {
        if (voteContent == null) return new String[0];
        if (voteContent.contains(",")) return Arrays.stream(voteContent.split(","))
            .map(String::trim).toArray(String[]::new);
        return Arrays.stream(voteContent.split("_"))
            .map(String::trim).toArray(String[]::new);
    }

    @Transactional
    public Post makePost(PostRequest.PostDto request, Long memberId) {
        Long groupId = null;
        PostType postType = PostType.from(request.type());

        String normalizedVoteContent = normalizeContent(request.voteContent());

        Post post = Post.builder()
            .content(request.content())
            .type(postType)
            .groupId(groupId)
                .eventId(request.eventId())
            .memberId(memberId)
            .voteTitle(request.voteTitle())
            .voteContent(normalizedVoteContent)
            .build();
        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(long id) {
        Post post = loadPostById(id);
        postRepository.delete(post);
    }

    @Transactional
    public PostResponse.PostDto updatePost(PostRequest.PostDto request, Long memberId, Long postId) {

        Post post = loadPostById(postId);

        String normalizedType = request.type() != null
            ? request.type().trim().toUpperCase()
            : post.getPostType().name();

        String voteTitle = normalizedType.equals("VOTE") ? request.voteTitle() : null;

        String voteContent = normalizedType.equals("VOTE")
            ? normalizeContent(request.voteContent())
            : null;

        PostRequest.PostDto normalizedRequest = new PostRequest.PostDto(
            request.groupId(),
            request.eventId(),
            normalizedType,
            request.content(),
            voteTitle,
            voteContent
        );

        post.updatePost(normalizedRequest);
        Post saved = postRepository.save(post);

        return PostResponse.PostDto.from(saved, memberId);
    }

    private Post loadPostById(long id) {
        return postRepository.findPostByPostId(id)
            .orElseThrow(() -> new BaseException(ErrorCode.POST_NOT_FOUND));
    }

    public PostResponse.PostListByGroupDto getPostByEvent(long eventId, Long memberId) {
        List<Post> posts = postRepository.findPostsByEventId(eventId);
        return PostResponse.PostListByGroupDto.from(posts, memberId);
    }

    public VoteLogResponseDto vote(PostRequest.VoteDto request, Long memberId) {

        Post post = loadPostById(request.postId());

        if (!post.getPostType().equals(PostType.VOTE)) {
            throw new BaseException(ErrorCode.POST_TYPE_NOT_VOTE);
        }

        Optional<VoteLog> checkLog = voteLogRepository.findVoteLogByMemberIdAndPost(memberId, post);
        if (checkLog.isPresent()) throw new BaseException(ErrorCode.VOTE_ALREADY_DO);

        String[] options = parseOptions(post.getVoteContent());

        int num = 0;
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(request.voteText())) num = i + 1;
        }

        VoteLog log = VoteLog.builder()
            .post(post)
            .memberId(memberId)
            .voteNum(num)
            .word(request.voteText())
            .build();

        post.addVoteLog(log);

        voteLogRepository.save(log);
        postRepository.save(post);

        return VoteLogResponseDto.from(post.getVoteLogs(), memberId);
    }

    @Transactional
    public void adminPost(PostRequest.AdminPostDto request, Long memberId) {

        List<Long> groupIds = Arrays.stream(request.groupNums().split("_"))
            .map(Long::parseLong)
            .toList();

        PostType postType = PostType.from(request.type());

        String normalizedVoteContent = normalizeContent(request.voteContent());

        List<Post> posts = new ArrayList<>();

        groupIds.forEach(g -> {
            Post post = Post.builder()
                .content(request.content())
                .type(postType)
                .groupId(g)
                .memberId(memberId)
                .voteTitle(request.voteTitle())
                .voteContent(normalizedVoteContent)
                .build();
            posts.add(post);
        });

        postRepository.saveAll(posts);
    }
}
