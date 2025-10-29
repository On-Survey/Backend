package OneQ.OnSurvey.domain.member.service;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.global.infra.toss.dto.DecryptedLoginMeResponse;

public interface MemberUpdater {
    void upsertMember(DecryptedLoginMeResponse decryptedLoginMeResponse);
    void changeMemberStatusTossConnectOut(Member member);
}
