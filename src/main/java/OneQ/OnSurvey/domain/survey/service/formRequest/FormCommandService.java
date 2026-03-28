package OneQ.OnSurvey.domain.survey.service.formRequest;

import OneQ.OnSurvey.domain.member.dto.MemberSearchResult;
import OneQ.OnSurvey.domain.member.service.MemberFinder;
import OneQ.OnSurvey.domain.survey.SurveyErrorCode;
import OneQ.OnSurvey.domain.survey.entity.FormRequest;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormPublishRequest;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormValidationPostResponse;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormValidationPayload;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormValidationRequestDto;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormValidationResponse;
import OneQ.OnSurvey.domain.survey.model.formRequest.event.FormRequestConversionEvent;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormRequestDto;
import OneQ.OnSurvey.domain.survey.model.response.SurveyFormResponse;
import OneQ.OnSurvey.domain.survey.repository.formRequest.FormRequestRepository;
import OneQ.OnSurvey.domain.survey.service.command.SurveyCommand;
import OneQ.OnSurvey.domain.survey.service.query.SurveyQueryService;
import OneQ.OnSurvey.global.common.exception.CustomException;
import OneQ.OnSurvey.global.infra.redis.RedisCacheAction;
import OneQ.OnSurvey.global.infra.redis.RedisLockAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.RedisException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static OneQ.OnSurvey.domain.survey.SurveyErrorCode.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FormCommandService implements FormCreator, FormUpdater, FormPublisher {
    private static final int EMAIL_QUOTA = 5;

    private final ApplicationEventPublisher eventPublisher;
    private final RedisCacheAction redisCacheAction;
    private final RedisLockAction redisLockAction;

    private final FormConverter formConverter;
    private final FormRequestLambda formRequestLambda;
    private final FormRequestRepository formRequestRepository;
    private final SurveyQueryService surveyQueryService;
    private final MemberFinder memberFinder;
    private final SurveyCommand surveyCommand;

    @Value("${redis.validation-key-prefix.lock:}")
    String validationLockPrefix;

    @Override
    public Long createFormRequest(Long userKey, Long memberId, FormRequestDto dto) {
        FormRequest request = dto.toEntity(userKey);
        FormRequest savedRequest = formRequestRepository.save(request);

        eventPublisher.publishEvent(new FormRequestConversionEvent(
            savedRequest.getId(),
            userKey,
            memberId,
            List.of(savedRequest.getFormLink()),
            dto.screening(),
            dto.surveyForm(),
            dto.interests()
        ));
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

    /**
     * 구글폼 링크 유효성을 검사한 뒤, 변환 가능/불가능한 문항 수를 각각 반환하고 반환 불가능한 문항에 대해서는 그 사유를 반환
     * 유효성 검사가 이루어진 데이터를 s3에 stash한다.
     *
     * @param dto 구글폼 링크 유효성 검사를 진행할 formLink는 필수로 가지고 있는 DTO
     * @return 변환된 문항 수 / 변환되지 않은 문항 및 사유
     */
    @Override
    public FormValidationResponse validationFormRequestLink(Long userKey, FormValidationRequestDto dto) {
        try {
            FormValidationPostResponse validationResult = redisLockAction.executeWithLock(
                validationLockPrefix + userKey,
                0,
                () -> {
                    String quotaKey = "ses:daily_usage:" + LocalDate.now() + ":" + userKey;
                    boolean isEmailRequired = Boolean.TRUE.equals(dto.isEmailRequired()) && EMAIL_QUOTA > redisCacheAction.getIntValue(quotaKey);

                    // 이메일을 요청했으나, 일일 한도를 초과한 경우
                    if (Boolean.TRUE.equals(dto.isEmailRequired()) && !isEmailRequired) {
                        throw new CustomException(SurveyErrorCode.FORM_VALIDATION_EMAIL_TOO_MANY_REQUEST);
                    }

                    FormValidationPayload payload = new FormValidationPayload(List.of(dto.formLink()), dto.requesterEmail(), isEmailRequired);
                    FormValidationPostResponse response = formRequestLambda.validateAndStashFormRequest(payload);

                    if (response == null) {
                        log.warn("[FORM:COMMAND:validationFormRequestLink] 구글폼 링크 유효성 검사 실패 - URL: {}", dto.formLink());
                        throw new CustomException(SurveyErrorCode.FORM_VALIDATION_FAILED);
                    }

                    if (response.isEmailSent()) {
                        boolean isFirstRequest = redisCacheAction.setValueIfAbsent(
                            quotaKey, "1",
                            Duration.between(LocalDateTime.now(), LocalDate.now().atStartOfDay().plusDays(1))
                        );
                        if (!isFirstRequest) {
                            redisCacheAction.incrementValue(quotaKey);
                        }
                    } else {
                        log.warn("[FORM:COMMAND:validationFormRequestLink] 링크 유효성 검사 후 이메일 발송 실패 - userKey: {}", userKey);
                    }

                    return response;
                });

            return formConverter.toResponse(validationResult);
        } catch (RedisException e) {
            log.warn("[FORM:COMMAND] 구글폼 링크 유효성 검사 락 획득 실패 - userKey: {}", userKey);
            throw new CustomException(SurveyErrorCode.FORM_VALIDATION_PROCEED);
        } catch (InterruptedException e) {
            log.warn("[FORM:COMMAND] 구글폼 링크 유효성 검사 락 획득 중 에러 발생 - userKey: {}", userKey);
            throw new CustomException(SurveyErrorCode.FORM_VALIDATION_FAILED);
        }
    }
}
