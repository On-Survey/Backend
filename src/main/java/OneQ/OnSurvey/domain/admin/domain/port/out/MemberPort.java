package OneQ.OnSurvey.domain.admin.domain.port.out;

import OneQ.OnSurvey.domain.admin.domain.model.member.AdminMemberView;

import java.util.List;

public interface MemberPort {

    Long validateAdminRoleAndGetMemberIdByUserKey(Long userKey);

    List<AdminMemberView> searchMembers(String email, String phoneNumber, Long memberId, String name);
}
