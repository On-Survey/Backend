package OneQ.OnSurvey.global.infra.toss.auth.service;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.service.MemberModifyService;
import OneQ.OnSurvey.domain.member.service.MemberQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WithdrawalService {

    private final MemberQueryService memberQueryService;
    private final MemberModifyService memberModifyService;

    @Transactional
    public void deleteAllInfo(Long userKey) {
        Member member = memberQueryService.getMemberByUserKey(userKey);
        Long memberId = member.getId();
        memberModifyService.deleteById(memberId);
    }
}
