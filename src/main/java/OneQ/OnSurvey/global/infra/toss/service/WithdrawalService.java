package OneQ.OnSurvey.global.infra.toss.service;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WithdrawalService {

    private final MemberService memberService;

    @Transactional
    public void deleteAllInfo(Long userKey) {
        Member member = memberService.getMemberByUserKey(userKey);
        Long memberId = member.getId();
        memberService.deleteById(memberId);
    }
}
