package OneQ.OnSurvey.domain.admin.domain.port.out;

import OneQ.OnSurvey.domain.member.dto.MemberSearchResult;

import java.util.List;

public interface MemberPort {

    Long validateAdminRoleAndGetMemberIdByUserKey(Long userKey);
    List<MemberSearchResult> searchMembers(String email, String phoneNumber, Long memberId, String name);
}
