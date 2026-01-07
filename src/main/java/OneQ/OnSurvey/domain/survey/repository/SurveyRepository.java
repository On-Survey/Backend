package OneQ.OnSurvey.domain.survey.repository;

import OneQ.OnSurvey.domain.member.dto.MemberSegmentation;
import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SurveyRepository {
    Optional<Survey> getSurveyById(Long surveyId);
    List<Survey> getSurveyListByMemberId(Long memberId);
    Slice<Survey> getSurveyListByFilters(
        Long lastSurveyId, LocalDateTime lastDeadline, Pageable pageable,
        SurveyStatus status, Long creatorId, Collection<Long> excludedIds, MemberSegmentation memberSegmentation,
        boolean filterByScreeningAnswer);

    Survey save(Survey survey);
}
