package OneQ.OnSurvey.domain.survey.repository.export;

import OneQ.OnSurvey.domain.survey.model.export.SurveyAnswerProjection;
import OneQ.OnSurvey.domain.survey.model.export.SurveyMemberProjection;
import OneQ.OnSurvey.domain.survey.model.export.SurveyQuestionHeader;

import java.util.List;

public interface SurveyExportRepository {
    List<SurveyQuestionHeader> findQuestionHeaders(Long surveyId);
    List<SurveyMemberProjection> findMembersWhoAnswered(Long surveyId);
    List<SurveyAnswerProjection> findAnswers(Long surveyId);
    String findSurveyTitle(Long surveyId);
    boolean existsOwnedSurvey(Long surveyId, Long ownerMemberId);
}
