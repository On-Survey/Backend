package OneQ.OnSurvey.domain.survey.service.formRequest;

import OneQ.OnSurvey.domain.survey.entity.FormRequest;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormRequestDto;
import OneQ.OnSurvey.domain.survey.repository.formRequest.FormRequestRepository;
import OneQ.OnSurvey.domain.survey.service.query.SurveyQueryService;
import OneQ.OnSurvey.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static OneQ.OnSurvey.domain.survey.SurveyErrorCode.FORM_REQUEST_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class FormCommandService implements FormCreator, FormUpdater {

    private final FormRequestRepository formRequestRepository;
    private final SurveyQueryService surveyQueryService;

    @Override
    public Long createFormRequest(FormRequestDto dto) {
        FormRequest request = dto.toEntity();
        FormRequest savedRequest = formRequestRepository.save(request);
        return savedRequest.getId();
    }

    @Override
    public void markAsRegistered(Long requestId, Long surveyId) {
        surveyQueryService.getSurveyById(surveyId);

        FormRequest request = formRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(FORM_REQUEST_NOT_FOUND));

        request.markAsRegistered(surveyId);
    }
}
