package OneQ.OnSurvey.domain.survey.service.formRequest;

import OneQ.OnSurvey.domain.member.dto.MemberSearchResult;
import OneQ.OnSurvey.domain.member.service.MemberFinder;
import OneQ.OnSurvey.domain.member.value.MemberStatus;
import OneQ.OnSurvey.domain.survey.SurveyErrorCode;
import OneQ.OnSurvey.domain.survey.entity.FormRequest;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormPublishRequest;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.response.SurveyFormResponse;
import OneQ.OnSurvey.domain.survey.repository.formRequest.FormRequestRepository;
import OneQ.OnSurvey.domain.survey.service.command.SurveyCommand;
import OneQ.OnSurvey.domain.survey.service.query.SurveyQueryService;
import OneQ.OnSurvey.global.common.exception.CustomException;
import OneQ.OnSurvey.global.infra.redis.RedisCacheAction;
import OneQ.OnSurvey.global.infra.redis.RedisLockAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FormCommandServiceTest {

    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private RedisCacheAction redisCacheAction;
    @Mock private RedisLockAction redisLockAction;
    @Mock private FormConverter formConverter;
    @Mock private FormRequestLambda formRequestLambda;
    @Mock private FormRequestRepository formRequestRepository;
    @Mock private SurveyQueryService surveyQueryService;
    @Mock private MemberFinder memberFinder;
    @Mock private SurveyCommand surveyCommand;

    @InjectMocks
    private FormCommandService formCommandService;

    private FormRequest buildRegisteredRequest() {
        FormRequest request = FormRequest.builder()
                .formLink("https://link")
                .userKey(1L)
                .requesterEmail("test@test.com")
                .isRegistered(true)
                .registeredSurveyId(10L)
                .build();
        return request;
    }

    private MemberSearchResult buildMemberResult(Long userKey) {
        return new MemberSearchResult(1L, userKey, "홍길동", "test@test.com",
                "010-1234-5678", "19900101", Gender.MALE, MemberStatus.ACTIVE, 1000L);
    }

    @Test
    @DisplayName("markAsRegistered - FormRequest 없으면 FORM_REQUEST_NOT_FOUND 예외")
    void markAsRegistered_notFound_throwsException() {
        given(formRequestRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> formCommandService.markAsRegistered(999L, 1L, 10))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(SurveyErrorCode.FORM_REQUEST_NOT_FOUND));
    }

    @Test
    @DisplayName("markAsRegistered - 성공 시 request에 surveyId와 questionCount 기록")
    void markAsRegistered_success_marksRequest() {
        FormRequest request = FormRequest.createRequest("https://link", "test@test.com", 100, 1000, 1L);
        given(formRequestRepository.findById(1L)).willReturn(Optional.of(request));

        formCommandService.markAsRegistered(1L, 10L, 5);

        assertThat(request.getIsRegistered()).isTrue();
        assertThat(request.getRegisteredSurveyId()).isEqualTo(10L);
        assertThat(request.getQuestionCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("publishFormRequest - FormRequest 없으면 FORM_REQUEST_NOT_FOUND 예외")
    void publishFormRequest_requestNotFound_throwsException() {
        given(formRequestRepository.findById(999L)).willReturn(Optional.empty());
        FormPublishRequest publishRequest = new FormPublishRequest(null, null, null);

        assertThatThrownBy(() -> formCommandService.publishFormRequest(999L, publishRequest))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(SurveyErrorCode.FORM_REQUEST_NOT_FOUND));
    }

    @Test
    @DisplayName("publishFormRequest - 미등록 상태면 FORM_REQUEST_NOT_YET_REGISTERED 예외")
    void publishFormRequest_notRegistered_throwsException() {
        FormRequest unregistered = FormRequest.createRequest("https://link", "test@test.com", 100, 1000, 1L);
        given(formRequestRepository.findById(1L)).willReturn(Optional.of(unregistered));
        FormPublishRequest publishRequest = new FormPublishRequest(null, null, null);

        assertThatThrownBy(() -> formCommandService.publishFormRequest(1L, publishRequest))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(SurveyErrorCode.FORM_REQUEST_NOT_YET_REGISTERED));
    }

    @Test
    @DisplayName("publishFormRequest - 회원 없으면 FORM_REQUEST_MEMBER_NOT_FOUND 예외")
    void publishFormRequest_memberNotFound_throwsException() {
        FormRequest registered = buildRegisteredRequest();
        given(formRequestRepository.findById(1L)).willReturn(Optional.of(registered));
        given(memberFinder.searchMembers("test@test.com", null, null, null)).willReturn(List.of());
        FormPublishRequest publishRequest = new FormPublishRequest(null, null, null);

        assertThatThrownBy(() -> formCommandService.publishFormRequest(1L, publishRequest))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(SurveyErrorCode.FORM_REQUEST_MEMBER_NOT_FOUND));
    }

    @Test
    @DisplayName("publishFormRequest - 회원이 복수면 FORM_REQUEST_MEMBER_NOT_FOUND 예외")
    void publishFormRequest_multipleMembers_throwsException() {
        FormRequest registered = buildRegisteredRequest();
        given(formRequestRepository.findById(1L)).willReturn(Optional.of(registered));
        given(memberFinder.searchMembers("test@test.com", null, null, null))
                .willReturn(List.of(buildMemberResult(1L), buildMemberResult(2L)));
        FormPublishRequest publishRequest = new FormPublishRequest(null, null, null);

        assertThatThrownBy(() -> formCommandService.publishFormRequest(1L, publishRequest))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(SurveyErrorCode.FORM_REQUEST_MEMBER_NOT_FOUND));
    }

    @Test
    @DisplayName("publishFormRequest - 성공 시 surveyCommand.submitSurvey 호출")
    void publishFormRequest_success_callsSubmitSurvey() {
        FormRequest registered = buildRegisteredRequest();
        given(formRequestRepository.findById(1L)).willReturn(Optional.of(registered));
        given(memberFinder.searchMembers("test@test.com", null, null, null))
                .willReturn(List.of(buildMemberResult(99L)));
        SurveyFormResponse mockResponse = SurveyFormResponse.builder().build();
        given(surveyCommand.submitSurvey(anyLong(), anyLong(), isNull())).willReturn(mockResponse);

        FormPublishRequest publishRequest = new FormPublishRequest(null, null, null);

        SurveyFormResponse result = formCommandService.publishFormRequest(1L, publishRequest);

        assertThat(result).isNotNull();
        verify(surveyCommand).submitSurvey(99L, 10L, null);
    }
}
