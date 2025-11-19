package OneQ.OnSurvey.domain.member.repository;

import OneQ.OnSurvey.domain.member.Member;

import java.util.Optional;

public interface MemberRepository {
    Optional<Member> findMemberByUserKey(Long userKey);
    Member save(Member newMember);
    void deleteById(Long memberId);

    Member findMemberInterestsById(Long memberId);
}
