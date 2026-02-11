package OneQ.OnSurvey.domain.admin.domain.port.out;

import OneQ.OnSurvey.domain.admin.api.dto.request.AdminSurveySearchQuery;
import OneQ.OnSurvey.domain.admin.domain.model.survey.AdminSurveyListView;
import OneQ.OnSurvey.domain.admin.domain.model.survey.SurveySingleViewInfo;
import OneQ.OnSurvey.domain.admin.domain.model.survey.SurveyQuestion;
import OneQ.OnSurvey.domain.admin.domain.model.survey.SurveyScreening;
import OneQ.OnSurvey.domain.admin.domain.model.survey.SurveySection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SurveyPort {

    Page<AdminSurveyListView> findPagedSurveyListByQuery(Pageable pageable, AdminSurveySearchQuery query);

    SurveySingleViewInfo findSurveyInformationById(Long surveyId);

    List<SurveyQuestion> findSurveyQuestionsById(Long surveyId);

    SurveyScreening findSurveyScreeningById(Long surveyId);

    List<SurveySection> findSurveySectionsById(Long surveyId);

    void updateSurveyOwner(Long surveyId, Long newMemberId);
}
