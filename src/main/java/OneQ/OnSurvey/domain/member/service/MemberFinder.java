package OneQ.OnSurvey.domain.member.service;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.dto.MemberInfoResponse;

public interface MemberFinder {
    Member getMemberByUserKey(Long userKey);
    MemberInfoResponse getMemberInfo(Long userKey);
}
