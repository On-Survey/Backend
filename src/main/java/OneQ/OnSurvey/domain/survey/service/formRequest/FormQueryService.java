package OneQ.OnSurvey.domain.survey.service.formRequest;

import OneQ.OnSurvey.domain.survey.entity.FormRequest;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormListResponse;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormRequestResponse;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormValidationEmailQuotaResponse;
import OneQ.OnSurvey.domain.survey.repository.formRequest.FormRequestRepository;
import OneQ.OnSurvey.global.infra.redis.RedisCacheAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FormQueryService implements FormFinder {
    private static final int EMAIL_QUOTA = 5;

    private final RedisCacheAction redisCacheAction;

    private final FormRequestRepository formRequestRepository;

    @Override
    public FormListResponse getAllUnregisteredRequests() {
        List<FormRequest> requests = formRequestRepository.findAllUnregistered();
        return FormListResponse.of(requests);
    }

    @Override
    public Page<FormRequestResponse> getFormRequests(String email, Boolean isRegistered, Pageable pageable) {
        Page<FormRequest> formRequestPage = formRequestRepository.findAllWithFilters(email, isRegistered, pageable);

        return formRequestPage.map(FormRequestResponse::of);
    }

    @Override
    public FormValidationEmailQuotaResponse getEmailQuota(Long userKey) {
        String quotaKey = "ses:daily_usage:" + LocalDate.now() + ":" + userKey;
        int usedCount = redisCacheAction.getIntValue(quotaKey);
        log.info("[FORM:FINDER] 이메일 수신 한도 잔량 userKey - {}, {}", userKey, EMAIL_QUOTA - usedCount);

        return new FormValidationEmailQuotaResponse(EMAIL_QUOTA - usedCount);
    }
}
