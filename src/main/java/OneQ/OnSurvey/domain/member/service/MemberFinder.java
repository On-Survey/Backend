package OneQ.OnSurvey.domain.member.service;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.dto.MemberInfoResponse;
import OneQ.OnSurvey.domain.member.dto.MemberSearchResult;

import java.util.List;

public interface MemberFinder {
    Member getMemberByUserKey(Long userKey);
    MemberInfoResponse getMemberInfo(Long userKey);

    Long validateAdminRoleAndGetMemberIdByUserKey(Long userKey);
    List<MemberSearchResult> searchMembers(String email, String phoneNumber, Long memberId, String name);
}
