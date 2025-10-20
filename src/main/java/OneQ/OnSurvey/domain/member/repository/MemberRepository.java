package OneQ.OnSurvey.domain.member.repository;

import OneQ.OnSurvey.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findMemberByUserKey(Long userKey);
}
