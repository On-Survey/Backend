package OneQ.OnSurvey.domain.member.service;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.repository.MemberRepository;
import OneQ.OnSurvey.domain.member.value.MemberStatus;
import OneQ.OnSurvey.domain.member.value.Role;
import OneQ.OnSurvey.global.infra.toss.dto.LoginMeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberModifyService implements MemberUpdater, MemberDeleter {

    private final MemberRepository memberRepository;

    @Override
    public Member upsertMember(LoginMeResponse.Success loginMe) {
        return memberRepository.findMemberByUserKey(loginMe.userKey())
                .map(existing -> {
                    existing.update(
                            loginMe.name(),
                            loginMe.phone(),
                            loginMe.birthday(),
                            loginMe.email(),
                            MemberStatus.ACTIVE
                    );

                    existing.updateAgreePolicy(loginMe.agreedTerms());
                    return existing;
                })
                .orElseGet(() -> {
                    Member newMember = Member.createMember(
                            loginMe.userKey(),
                            loginMe.name(),
                            loginMe.phone(),
                            loginMe.birthday(),
                            loginMe.email(),
                            Role.ROLE_MEMBER,
                            MemberStatus.ACTIVE
                    );

                    newMember.updateAgreePolicy(loginMe.agreedTerms());
                    return memberRepository.save(newMember);
                });
    }

    @Override
    public void changeMemberStatusTossConnectOut(Member member) {
        member.memberConnectOut();
        memberRepository.save(member);
    }

    @Override
    public Boolean deleteById(Long memberId) {
        memberRepository.deleteById(memberId);
        return true;
    }
}
