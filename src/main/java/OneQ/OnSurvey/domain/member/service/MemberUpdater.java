package OneQ.OnSurvey.domain.member.service;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.global.infra.toss.dto.LoginMeResponse;

public interface MemberUpdater {
    Member upsertMember(LoginMeResponse.Success loginMe);
    void changeMemberStatusTossConnectOut(Member member);
}
