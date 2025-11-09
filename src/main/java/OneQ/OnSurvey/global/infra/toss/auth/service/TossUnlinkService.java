package OneQ.OnSurvey.global.infra.toss.auth.service;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.service.MemberModifyService;
import OneQ.OnSurvey.domain.member.service.MemberQueryService;
import OneQ.OnSurvey.global.auth.token.TokenStore;
import OneQ.OnSurvey.global.exception.CustomException;
import OneQ.OnSurvey.global.infra.toss.auth.TossUnlinkValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static OneQ.OnSurvey.global.infra.toss.common.TossErrorCode.INVALID_REFERRER;

@Service
@RequiredArgsConstructor
public class TossUnlinkService {

    private final WithdrawalService withdrawalService;
    private final MemberQueryService memberQueryService;
    private final MemberModifyService memberModifyService;
    private final TokenStore tokenStore;

    @Transactional
    public void unlink(Long userKey, TossUnlinkValue referrer) {
        Member member = memberQueryService.getMemberByUserKey(userKey);
        if(referrer == TossUnlinkValue.UNLINK) {
            tokenStore.deleteRefresh(userKey); // 리프레시 토큰 만료
            memberModifyService.changeMemberStatusTossConnectOut(member); // 멤버 status TOSS_CONNECT_OUT으로 : accessToken으로도 로그인 못하게
        }
        else if(referrer == TossUnlinkValue.WITHDRAWAL_TOSS || referrer == TossUnlinkValue.WITHDRAWAL_TERMS) {
            withdrawalService.deleteAllInfo(userKey); // 탈퇴 처리 -> 관련된 유저의 모든 데이터 삭제
        }
        else {
            throw new CustomException(INVALID_REFERRER);
        }
    }
}
