package OneQ.OnSurvey.domain.member.repository;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.value.Interest;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

import static OneQ.OnSurvey.domain.member.QMember.member;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Member> findMemberByUserKey(Long userKey) {
        return memberJpaRepository.findMemberByUserKey(userKey);
    }

    @Override
    public Member save(Member newMember) {
        return memberJpaRepository.save(newMember);
    }

    @Override
    public void deleteById(Long memberId) {
        memberJpaRepository.deleteById(memberId);
    }

    @Override
    public Set<Interest> findMemberInterestsById(Long memberId) {
        return jpaQueryFactory.select(member.interests)
            .from(member)
            .leftJoin(member.interests).fetchJoin()
            .where(member.id.eq(memberId))
            .fetchOne();
    }
}
