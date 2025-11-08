package OneQ.OnSurvey.domain.survey.service;

import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.domain.survey.model.response.SurveyManagementDetailResponse;
import OneQ.OnSurvey.domain.survey.model.response.SurveyManagementResponse;
import OneQ.OnSurvey.domain.survey.model.response.SurveyParticipationResponse;
import OneQ.OnSurvey.domain.survey.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SurveyQueryService implements SurveyQuery {
    private final SurveyRepository surveyRepository;

    @Override
    public SurveyManagementDetailResponse getSurvey(Long surveyId) {
        Survey survey = surveyRepository.getSurveyById(surveyId);

        return new SurveyManagementDetailResponse(survey.getId(), survey.getMemberId(), survey.getStatus());
    }

    @Override
    public List<SurveyManagementResponse.SurveyInfo> getSurveyListByMemberId(Long memberId) {
        List<Survey> surveyList = surveyRepository.getSurveyListByMemberId(memberId);

        return surveyList.stream().map(SurveyManagementResponse::fromEntity).toList();
    }

    @Override
    public SurveyParticipationResponse getParticipationSurveyList(
        SurveyStatus status,
        Long lastSurveyId,
        Pageable pageable
    ) {
        List<Survey> surveyList = surveyRepository.getSurveyList(status, lastSurveyId, pageable);

        return SurveyParticipationResponse.builder()
            .recommended(surveyList.stream().map(SurveyParticipationResponse::fromEntity).toList())
            .impending(surveyList.stream().map(SurveyParticipationResponse::fromEntity).toList())
            .build();
    }
}
