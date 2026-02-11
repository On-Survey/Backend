package OneQ.OnSurvey.domain.member.repository;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.dto.MemberSegmentation;
import OneQ.OnSurvey.domain.member.value.Role;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static OneQ.OnSurvey.domain.member.QMember.member;
import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.set;

@Slf4j
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
        MemberSegmentation memberSegmentation = jpaQueryFactory.selectFrom(member)
            .leftJoin(member.interests)
            .where(member.userKey.eq(userKey))
            .transform(groupBy(member.userKey).as(
                Projections.fields(
                    MemberSegmentation.class,
                    member.gender,
                    member.birthDay,
                    member.residence,
                    set(member.interests).as("interests")
                )
            ))
            .get(userKey);

        if (memberSegmentation == null) {
            log.warn("[MEMBER:REPOSITORY:findMemberSegmentByUserKey] 해당 유저의 관심사가 등록되지 않았습니다. userKey = {}", userKey);
            return jpaQueryFactory.select(
                Projections.fields(
                    MemberSegmentation.class,
                    member.gender,
                    member.birthDay,
                    member.residence
                ))
                .from(member)
                .where(member.userKey.eq(userKey))
                .fetchOne();
        }

        return memberSegmentation;
    }

    @Override
    public Long validateAdminRoleAndGetMemberIdByUserKey(Long userKey) {
        return jpaQueryFactory.select(member.id)
            .from(member)
            .where(
                member.userKey.eq(userKey),
                member.role.eq(Role.ROLE_ADMIN)
            )
            .fetchFirst();
    }

    @Override
    public List<Member> searchMembers(String email, String phoneNumber, Long memberId, String name) {
        BooleanBuilder builder = new BooleanBuilder();

        if (email != null && !email.isBlank()) {
            builder.and(member.email.containsIgnoreCase(email));
        }
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            builder.and(member.phoneNumber.contains(phoneNumber));
        }
        if (memberId != null) {
            builder.and(member.id.eq(memberId));
        }
        if (name != null && !name.isBlank()) {
            builder.and(member.name.containsIgnoreCase(name));
        }

        return jpaQueryFactory.selectFrom(member)
            .where(builder)
            .orderBy(member.id.desc())
            .limit(100)
            .fetch();
    }
}
