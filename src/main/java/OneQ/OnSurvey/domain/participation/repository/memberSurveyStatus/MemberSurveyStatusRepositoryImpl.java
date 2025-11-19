package OneQ.OnSurvey.domain.participation.repository.memberSurveyStatus;

import OneQ.OnSurvey.domain.participation.entity.MemberSurveyStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Repository;

import static OneQ.OnSurvey.domain.participation.entity.QMemberSurveyStatus.memberSurveyStatus;

@Repository
@RequiredArgsConstructor
public class MemberSurveyStatusRepositoryImpl implements MemberSurveyStatusRepository {
    private final MemberSurveyStatusJpaRepository jpaRepository;

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Long> getExcludedSurveyIdList(Long memberId, boolean checkScreened) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(memberSurveyStatus.memberId.eq(memberId));
        // 스크리닝 응답 여부를 확인하는 경우에만 조건 추가
        if (checkScreened) {
            builder.and(memberSurveyStatus.isScreened.eq(true));
        }

        return jpaQueryFactory.select(memberSurveyStatus.surveyId)
            .from(memberSurveyStatus)
            .where(builder)
            .fetch();
    }

    @Override
    public MemberSurveyStatus getMemberSurveyStatus(Long surveyId, Long memberId) {
        return jpaQueryFactory.selectFrom(memberSurveyStatus)
            .where(memberSurveyStatus.surveyId.eq(surveyId)
                .and(memberSurveyStatus.memberId.eq(memberId)))
            .fetchOne();
    }

    @Override
    public MemberSurveyStatus save(MemberSurveyStatus memberSurveyStatus) {
        return jpaRepository.save(memberSurveyStatus);
    }
}
