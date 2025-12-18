package OneQ.OnSurvey.domain.member.repository;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.dto.MemberSegmentation;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
    public Member findMemberInterestsById(Long memberId) {
        return jpaQueryFactory.selectFrom(member)
            .leftJoin(member.interests).fetchJoin()
            .where(member.id.eq(memberId))
            .fetchOne();
    }

    @Override
    public MemberSegmentation findMemberSegmentByUserKey(Long userKey) {

        Member info = jpaQueryFactory.selectFrom(member)
            .leftJoin(member.interests).fetchJoin()
            .where(member.id.eq(userKey))
            .fetchOne();

        if (info == null) {
            return null;
        }

        return new MemberSegmentation(
            info.getGender(),
            info.getBirthDay(),
            info.getResidence(),
            info.getInterests()
        );
    }
}
