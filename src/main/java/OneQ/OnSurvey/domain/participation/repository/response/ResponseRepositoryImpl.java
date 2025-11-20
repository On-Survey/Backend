package OneQ.OnSurvey.domain.participation.repository.response;

import OneQ.OnSurvey.domain.participation.entity.Response;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static OneQ.OnSurvey.domain.participation.entity.QResponse.response;

@Repository
@RequiredArgsConstructor
public class ResponseRepositoryImpl implements ResponseRepository {
    private final ResponseJpaRepository responseJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Response> getResponsesByMemberId(Long memberId) {
        return responseJpaRepository.getResponsesByMemberId(memberId);
    }

    @Override
    public List<Response> getResponsesBySurveyId(Long surveyId) {
        return responseJpaRepository.getResponsesBySurveyId(surveyId);
    }

    @Override
    public Response getResponseBySurveyIdAndMemberId(Long surveyId, Long memberId) {
        return jpaQueryFactory.selectFrom(response)
            .where(
                response.surveyId.eq(surveyId),
                response.memberId.eq(memberId)
            )
            .fetchOne();
    }

    @Override
    public Integer getResponseCountBySurveyId(Long surveyId) {
        return responseJpaRepository.countResponsesBySurveyId(surveyId);
    }

    @Override
    public Map<Long, Long> getResponseCountsBySurveyIds(Collection<Long> surveyIds) {
        return jpaQueryFactory.select(response.surveyId, response.surveyId.count())
            .from(response)
            .where(response.surveyId.in(surveyIds))
            .groupBy(response.surveyId)
            .fetch()
            .stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get(response.surveyId),
                tuple -> tuple.get(response.surveyId.count())
            ));
    }

    @Override
    public Response save(Response response) {
        return responseJpaRepository.save(response);
    }

    @Override
    public boolean existsBySurveyIdAndMemberId(Long surveyId, Long memberId) {
        return responseJpaRepository.existsBySurveyIdAndMemberId(surveyId, memberId);
    }
}
