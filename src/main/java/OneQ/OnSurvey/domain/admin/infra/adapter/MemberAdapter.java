package OneQ.OnSurvey.domain.admin.infra.adapter;

import OneQ.OnSurvey.domain.admin.domain.port.out.MemberPort;
import OneQ.OnSurvey.domain.member.service.MemberFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberAdapter implements MemberPort {

    private final MemberFinder memberFinder;

    public Long validateAdminRoleAndGetMemberIdByUserKey(Long userKey) {
         return memberFinder.validateAdminRoleAndGetMemberIdByUserKey(userKey);
    }
}
