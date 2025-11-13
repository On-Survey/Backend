package OneQ.OnSurvey.domain.member.service;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.global.infra.toss.auth.dto.DecryptedLoginMeResponse;
import OneQ.OnSurvey.domain.survey.model.Residence;

import java.util.Set;

public interface MemberUpdater {
    Member upsertMember(DecryptedLoginMeResponse decryptedLoginMeResponse);
    void changeMemberStatusTossConnectOut(Member member);
    void changeProfileImage(Long userKey, String profileImageUrl);
    void completeOnboarding(Long userKey, Residence residence, Set<Interest> interests);
}
