package OneQ.OnSurvey.domain.survey.service;

import OneQ.OnSurvey.domain.member.service.MemberFinder;
import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.repository.SurveyRepository;
import OneQ.OnSurvey.global.auth.custom.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SurveyCommandService implements SurveyCommand {
    private final SurveyRepository surveyRepository;
    private final MemberFinder memberFinder;

    @Override
    public Survey upsertSurvey(Long surveyId, String title, String description) {
        if (surveyId == null) {
            CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long userKey = customUserDetails.getUserKey();

            Survey survey = Survey.of(
                memberFinder.getMemberByUserKey(userKey).getId(),
                title,
                description
            );

            return surveyRepository.save(survey);
        }

        Survey survey = surveyRepository.getSurveyById(surveyId);
        survey.updateSurveyTitleAndDescription(title, description);
        return surveyRepository.save(survey);
    }

    @Override
    public Boolean deleteById(Long surveyId) {
        surveyRepository.deleteById(surveyId);
        return true;
    }
}
