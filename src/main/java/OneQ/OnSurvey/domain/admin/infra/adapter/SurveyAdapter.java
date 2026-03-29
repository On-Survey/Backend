package OneQ.OnSurvey.domain.admin.infra.adapter;

import OneQ.OnSurvey.domain.admin.api.dto.request.AdminSurveySearchQuery;
import OneQ.OnSurvey.domain.admin.domain.model.survey.AdminSurveyListView;
import OneQ.OnSurvey.domain.admin.domain.model.survey.OngoingSurveyView;
import OneQ.OnSurvey.domain.admin.domain.model.survey.SurveySingleViewInfo;
import OneQ.OnSurvey.domain.admin.domain.model.survey.SurveyQuestion;
import OneQ.OnSurvey.domain.admin.domain.model.survey.SurveyScreening;
import OneQ.OnSurvey.domain.admin.domain.model.survey.SurveySection;
import OneQ.OnSurvey.domain.admin.domain.port.out.SurveyPort;
import OneQ.OnSurvey.domain.admin.infra.mapper.AdminSurveyMapper;
import OneQ.OnSurvey.domain.question.model.dto.SectionDto;
import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;
import OneQ.OnSurvey.domain.question.service.QuestionQuery;
import OneQ.OnSurvey.domain.survey.model.dto.ScreeningViewData;
import OneQ.OnSurvey.domain.survey.model.dto.OngoingSurveyStats;
import OneQ.OnSurvey.domain.survey.model.dto.SurveyDetailData;
import OneQ.OnSurvey.domain.survey.model.dto.SurveyListView;
import OneQ.OnSurvey.domain.survey.model.dto.SurveyOwnerChangeDto;
import OneQ.OnSurvey.domain.survey.model.dto.SurveySearchQuery;
import OneQ.OnSurvey.domain.survey.service.command.SurveyCommand;
import OneQ.OnSurvey.domain.survey.service.query.SurveyQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SurveyAdapter implements SurveyPort {

    private final SurveyCommand surveyCommandService;
    private final SurveyQuery surveyQueryService;
    private final QuestionQuery questionQueryService;

    @Override
    public Page<AdminSurveyListView> findPagedSurveyListByQuery(Pageable pageable, AdminSurveySearchQuery query) {

        SurveySearchQuery surveySearchQuery = SurveySearchQuery.builder()
            .status(AdminSurveyMapper.toSurveyStatus(query.status()))
            .title(query.keyword())
            .creator(query.creator())
            .build();

        Page<SurveyListView> pagedSurveyAdminListView = surveyQueryService.getPagedSurveyListViewByQuery(
            pageable, surveySearchQuery
        );

        return pagedSurveyAdminListView.map(AdminSurveyMapper::toAdminSurveyListView);
    }

    @Override
    public SurveySingleViewInfo findSurveyInformationById(Long surveyId) {
        SurveyDetailData surveyDetailData = surveyQueryService.getSurveyDetailById(surveyId);

        return AdminSurveyMapper.toSurveySingleViewInfo(surveyDetailData);
    }

    @Override
    public List<SurveyQuestion> findSurveyQuestionsById(Long surveyId) {
        List<DefaultQuestionDto> questionDtoList = questionQueryService.getQuestionDtoListBySurveyId(surveyId);

        return questionDtoList.stream()
            .map(AdminSurveyMapper::toSurveyQuestion)
            .toList();
    }

    @Override
    public SurveyScreening findSurveyScreeningById(Long surveyId) {
        ScreeningViewData screeningIntroData = surveyQueryService.getScreeningIntroBySurveyId(surveyId);

        return AdminSurveyMapper.toSurveyScreening(screeningIntroData);
    }

    @Override
    public List<SurveySection> findSurveySectionsById(Long surveyId) {
        List<SectionDto> sectionDtoList = surveyQueryService.getSectionDtoListBySurveyId(surveyId);

        return sectionDtoList.stream()
            .map(AdminSurveyMapper::toSurveySection)
            .toList();
    }

    @Override
    public List<OngoingSurveyView> findOngoingSurveys() {
        List<OngoingSurveyStats> statsList = surveyQueryService.getOngoingSurveyStats();
        return statsList.stream()
            .map(s -> new OngoingSurveyView(
                s.getSurveyId(),
                s.getTitle(),
                s.getCompletedCount() != null ? s.getCompletedCount() : 0,
                s.getDueCount() != null ? s.getDueCount() : 0
            ))
            .toList();
    }

    @Override
    public void updateSurveyOwner(Long surveyId, Long memberId) {
        SurveyOwnerChangeDto changeDto = SurveyOwnerChangeDto.builder()
            .surveyId(surveyId)
            .newMemberId(memberId)
            .build();

        surveyCommandService.updateSurveyOwner(changeDto);
    }
}
