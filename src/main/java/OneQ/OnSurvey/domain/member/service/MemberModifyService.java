package OneQ.OnSurvey.domain.member.service;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.MemberErrorCode;
import OneQ.OnSurvey.domain.member.repository.MemberRepository;
import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.member.value.MemberStatus;
import OneQ.OnSurvey.domain.member.value.Role;
import OneQ.OnSurvey.global.exception.CustomException;
import OneQ.OnSurvey.global.infra.toss.auth.dto.DecryptedLoginMeResponse;
import OneQ.OnSurvey.domain.survey.model.Residence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberModifyService implements MemberUpdater, MemberDeleter {

    private final MemberRepository memberRepository;

    @Override
    public Member upsertMember(DecryptedLoginMeResponse decryptedLoginMeResponse) {
        return memberRepository.findMemberByUserKey(decryptedLoginMeResponse.userKey())
                .map(existing -> {
                    existing.update(
                            decryptedLoginMeResponse.name(),
                            decryptedLoginMeResponse.phone(),
                            decryptedLoginMeResponse.birthday(),
                            decryptedLoginMeResponse.email(),
                            decryptedLoginMeResponse.gender(),
                            MemberStatus.ACTIVE
                    );

                    existing.updateAgreePolicy(decryptedLoginMeResponse.agreedTerms());
                    return existing;
                })
                .orElseGet(() -> {
                    Member newMember = Member.createMember(
                            decryptedLoginMeResponse.userKey(),
                            decryptedLoginMeResponse.name(),
                            decryptedLoginMeResponse.phone(),
                            decryptedLoginMeResponse.birthday(),
                            decryptedLoginMeResponse.email(),
                            decryptedLoginMeResponse.gender(),
                            Role.ROLE_MEMBER,
                            MemberStatus.ACTIVE
                    );

                    newMember.updateAgreePolicy(decryptedLoginMeResponse.agreedTerms());
                    return memberRepository.save(newMember);
                });
    }

    @Override
    public void changeMemberStatusTossConnectOut(Member member) {
        member.memberConnectOut();
        memberRepository.save(member);
    }

    @Override
    public void changeProfileImage(Long userKey, String profileImageUrl) {
        Member member = memberRepository.findMemberByUserKey(userKey)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));
        member.changeProfileUrl(profileImageUrl);
    }

    @Override
    public void completeOnboarding(Long userKey, Residence residence, Set<Interest> interests) {
        Member member = memberRepository.findMemberByUserKey(userKey)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

        member.completeOnboarding(residence, interests);
    }

    @Override
    public Boolean deleteById(Long memberId) {
        memberRepository.deleteById(memberId);
        return true;
    }
}
