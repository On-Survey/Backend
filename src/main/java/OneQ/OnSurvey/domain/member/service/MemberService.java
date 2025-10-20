package OneQ.OnSurvey.domain.member.service;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.repository.MemberRepository;
import OneQ.OnSurvey.domain.member.value.MemberStatus;
import OneQ.OnSurvey.domain.member.value.Role;
import OneQ.OnSurvey.global.exception.CustomException;
import OneQ.OnSurvey.global.infra.toss.dto.LoginMeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static OneQ.OnSurvey.domain.member.MemberErrorCode.MEMBER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
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

    private Optional<Member> findMemberByUserKey(Long userKey) {
        return memberRepository.findMemberByUserKey(userKey);
    }

    @Transactional(readOnly = true)
    public Member getMemberByUserKey(Long userKey) {
        return findMemberByUserKey(userKey).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
    }

    @Transactional
    public Member save(Member member) {
        return memberRepository.save(member);
    }

    @Transactional
    public Boolean deleteById(Long memberId) {
        memberRepository.deleteById(memberId);
        return true;
    }

    @Transactional
    public void changeMemberStatusTossConnectOut(Member member) {
        member.memberConnectOut();
        save(member);
    }
}
