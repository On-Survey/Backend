package OneQ.OnSurvey.domain.admin.infra.adapter;

import OneQ.OnSurvey.domain.admin.domain.port.out.MemberPort;
import OneQ.OnSurvey.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberAdapter implements MemberPort {

    private final MemberRepository memberRepository;

    public Long validateAdminRoleAndGetMemberIdByUserKey(Long userKey) {
         return memberRepository.validateAdminRoleAndGetMemberIdByUserKey(userKey);
    }
}
