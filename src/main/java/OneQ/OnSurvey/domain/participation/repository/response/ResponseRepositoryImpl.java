package OneQ.OnSurvey.domain.participation.repository.response;

import OneQ.OnSurvey.domain.participation.entity.Response;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    public Response save(Response response) {
        return responseJpaRepository.save(response);
    }

    @Override
    public boolean existsBySurveyIdAndMemberId(Long surveyId, Long memberId) {
        return responseJpaRepository.existsBySurveyIdAndMemberId(surveyId, memberId);
    }
}
