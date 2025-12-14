package OneQ.OnSurvey.domain.member.service;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.MemberErrorCode;
import OneQ.OnSurvey.domain.member.dto.MemberInfoResponse;
import OneQ.OnSurvey.domain.member.repository.MemberRepository;
import OneQ.OnSurvey.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static OneQ.OnSurvey.domain.member.MemberErrorCode.MEMBER_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberQueryService implements MemberFinder {

    private final MemberRepository memberRepository;

    @Override
    public Member getMemberByUserKey(Long userKey) {
        return memberRepository.findMemberByUserKey(userKey)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
    }

    @Override
    public MemberInfoResponse getMemberInfo(Long userKey) {
        Member member = memberRepository.findMemberByUserKey(userKey)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

        return MemberInfoResponse.of(member.getName(), member.getProfileUrl(), member.getCoin(), member.getPromotionPoint());
    }
}
