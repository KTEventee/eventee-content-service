package eventee.server.content.domain.client.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberQueryAdapter implements MemberQueryPort {

    private final MemberClient memberClient;

    @Override
    public MemberListDto.MemberDto getMember(Long memberId) {
        MemberListDto.MemberDto dto = memberClient.getMember(memberId);
        return dto;
    }
}
