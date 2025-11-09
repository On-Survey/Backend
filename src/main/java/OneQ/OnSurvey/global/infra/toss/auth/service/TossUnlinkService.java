package OneQ.OnSurvey.global.infra.toss.auth.service;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.service.MemberModifyService;
import OneQ.OnSurvey.domain.member.service.MemberQueryService;
import OneQ.OnSurvey.global.exception.CustomException;
import OneQ.OnSurvey.global.infra.toss.auth.TossUnlinkValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static OneQ.OnSurvey.global.infra.toss.common.TossErrorCode.INVALID_REFERRER;

@Slf4j
@Service
@RequiredArgsConstructor
public class TossUnlinkService {

    private final WithdrawalService withdrawalService;
    private final MemberQueryService memberQueryService;
    private final MemberModifyService memberModifyService;
    private final TossAuthService tossAuthService;
    /**
     * 토스 연결 해지
     *  - UNLINK: 토스 연결만 해지, 멤버는 유지
     *  - WITHDRAWAL_TOSS / WITHDRAWAL_TERMS: 토스 연결 해지 후 서비스 탈퇴
     */
    @Transactional
    public void unlink(Long userKey, TossUnlinkValue referrer) {
        Member member = memberQueryService.getMemberByUserKey(userKey);

        try {
            tossAuthService.logoutByUserKey(userKey);
        } catch (Exception e) {
            log.warn("[TossUnlinkService] removeByUserKey failed: userKey={}, err={}", userKey, e.toString());
        }

        switch (referrer) {
            case UNLINK -> memberModifyService.changeMemberStatusTossConnectOut(member);
            case WITHDRAWAL_TOSS, WITHDRAWAL_TERMS -> withdrawalService.deleteAllInfo(userKey);
            default -> throw new CustomException(INVALID_REFERRER);
        }
    }
}
