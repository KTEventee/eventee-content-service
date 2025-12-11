package eventee.server.content.domain.client.member;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "member-service", url = "${member-service.url}")
public interface MemberClient {
    @GetMapping("/internal/members/{id}")
    MemberListDto.MemberDto getMember(@PathVariable Long id);
}