package OneQ.OnSurvey.domain.member.service;

import OneQ.OnSurvey.domain.member.Member;

public interface MemberFinder {
    Member getMemberByUserKey(Long userKey);
}
