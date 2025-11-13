package OneQ.OnSurvey.domain.member.service;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.global.infra.toss.auth.dto.DecryptedLoginMeResponse;

public interface MemberUpdater {
    void upsertMember(DecryptedLoginMeResponse decryptedLoginMeResponse);
    void changeMemberStatusTossConnectOut(Member member);
    void changeProfileImage(Long userKey, String profileImageUrl);
}
