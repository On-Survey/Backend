package OneQ.OnSurvey.domain.survey.service.formRequest;

import OneQ.OnSurvey.domain.member.dto.MemberSearchResult;
import OneQ.OnSurvey.domain.member.service.MemberFinder;
import OneQ.OnSurvey.domain.survey.entity.FormRequest;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormPublishRequest;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormRequestDto;
import OneQ.OnSurvey.domain.survey.model.formRequest.event.FormRequestConversionEvent;
import OneQ.OnSurvey.domain.survey.model.response.SurveyFormResponse;
import OneQ.OnSurvey.domain.survey.repository.formRequest.FormRequestRepository;
import OneQ.OnSurvey.domain.survey.service.command.SurveyCommand;
import OneQ.OnSurvey.domain.survey.service.query.SurveyQueryService;
import OneQ.OnSurvey.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static OneQ.OnSurvey.domain.survey.SurveyErrorCode.*;

@Service
@Transactional
@RequiredArgsConstructor
public class FormCommandService implements FormCreator, FormUpdater, FormPublisher {

    private final ApplicationEventPublisher eventPublisher;
    private final FormRequestRepository formRequestRepository;
    private final SurveyQueryService surveyQueryService;
    private final MemberFinder memberFinder;
    private final SurveyCommand surveyCommand;

    @Override
    public Long createFormRequest(FormRequestDto dto) {
        FormRequest request = dto.toEntity();
        FormRequest savedRequest = formRequestRepository.save(request);

        eventPublisher.publishEvent(new FormRequestConversionEvent(
            savedRequest.getId(),
            savedRequest.getRequesterEmail(),
            List.of(savedRequest.getFormLink()))
        );
        return savedRequest.getId();
    }

    @Override
    public void markAsRegistered(Long requestId, Long surveyId, Integer questionCount) {
        surveyQueryService.getSurveyById(surveyId);

        FormRequest request = formRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(FORM_REQUEST_NOT_FOUND));

        request.markAsRegistered(surveyId, questionCount);
    }

    @Override
    public SurveyFormResponse publishFormRequest(Long requestId, FormPublishRequest publishRequest) {
        FormRequest formRequest = formRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(FORM_REQUEST_NOT_FOUND));

        if (!formRequest.getIsRegistered() || formRequest.getRegisteredSurveyId() == null) {
            throw new CustomException(FORM_REQUEST_NOT_YET_REGISTERED);
        }

        Long surveyId = formRequest.getRegisteredSurveyId();

        List<MemberSearchResult> members = memberFinder.searchMembers(formRequest.getRequesterEmail(), null, null, null);
        if (members.size() != 1) {
            throw new CustomException(FORM_REQUEST_MEMBER_NOT_FOUND);
        }
        Long userKey = members.getFirst().userKey();

        if (publishRequest.screening() != null) {
            surveyCommand.upsertScreening(
                surveyId,
                publishRequest.screening().content(),
                publishRequest.screening().answer()
            );
        }

        return surveyCommand.submitSurvey(userKey, surveyId, publishRequest.surveyForm());
    }
}
