package OneQ.OnSurvey.domain.admin.infra.adapter;

import OneQ.OnSurvey.domain.admin.domain.model.member.AdminMemberView;
import OneQ.OnSurvey.domain.admin.domain.port.out.MemberPort;
import OneQ.OnSurvey.domain.admin.infra.mapper.AdminMemberMapper;
import OneQ.OnSurvey.domain.member.dto.MemberSearchResult;
import OneQ.OnSurvey.domain.member.service.MemberFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MemberAdapter implements MemberPort {

    private final MemberFinder memberFinder;

    @Override
    public Long validateAdminRoleAndGetMemberIdByUserKey(Long userKey) {
         return memberFinder.validateAdminRoleAndGetMemberIdByUserKey(userKey);
    }

    @Override
    public List<AdminMemberView> searchMembers(String email, String phoneNumber, Long memberId, String name) {
        List<MemberSearchResult> results = memberFinder.searchMembers(email, phoneNumber, memberId, name);

        return results.stream().map(AdminMemberMapper::toAdminMemberView).toList();
    }
}
