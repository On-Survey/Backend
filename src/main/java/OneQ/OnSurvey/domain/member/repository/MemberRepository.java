package OneQ.OnSurvey.domain.member.repository;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.dto.MemberSegmentation;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    Optional<Member> findMemberByUserKey(Long userKey);
    Member save(Member newMember);
    void deleteById(Long memberId);

    Member findMemberInterestsById(Long memberId);
    MemberSegmentation findMemberSegmentByUserKey(Long userKey);

    Long validateAdminRoleAndGetMemberIdByUserKey(Long userKey);
    List<Member> searchMembers(String email, String phoneNumber, Long memberId, String name);
    String getUsernameByUserKey(Long userKey);
}
