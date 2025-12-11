package eventee.server.content.domain.client.member;

public interface MemberQueryPort {
    MemberListDto.MemberDto getMember(Long memberId);
}