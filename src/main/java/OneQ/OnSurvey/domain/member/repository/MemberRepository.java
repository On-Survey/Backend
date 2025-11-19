package OneQ.OnSurvey.domain.member.repository;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.value.Interest;

import java.util.Optional;
import java.util.Set;

public interface MemberRepository {
    Optional<Member> findMemberByUserKey(Long userKey);
    Member save(Member newMember);
    void deleteById(Long memberId);

    Set<Interest> findMemberInterestsById(Long memberId);
}
