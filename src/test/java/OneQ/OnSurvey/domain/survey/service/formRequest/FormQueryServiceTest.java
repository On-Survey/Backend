package OneQ.OnSurvey.domain.survey.service.formRequest;

import OneQ.OnSurvey.domain.survey.entity.FormRequest;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormListResponse;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormValidationEmailQuotaResponse;
import OneQ.OnSurvey.domain.survey.repository.formRequest.FormRequestRepository;
import OneQ.OnSurvey.global.infra.redis.RedisCacheAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FormQueryServiceTest {

    @Mock private RedisCacheAction redisCacheAction;
    @Mock private FormRequestRepository formRequestRepository;

    @InjectMocks
    private FormQueryService formQueryService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(formQueryService, "emailQuota", 20);
        ReflectionTestUtils.setField(formQueryService, "emailHourUsageKey", "form:email:usage:");
    }

    @Test
    @DisplayName("getEmailQuota - 사용량 없으면 전체 한도 반환")
    void getEmailQuota_noUsage_returnsFullQuota() {
        given(redisCacheAction.getIntValue(anyString())).willReturn(0);

        FormValidationEmailQuotaResponse result = formQueryService.getEmailQuota(1L);

        assertThat(result.quota()).isEqualTo(20);
    }

    @Test
    @DisplayName("getEmailQuota - 사용량이 있으면 차감된 한도 반환")
    void getEmailQuota_withUsage_returnsReducedQuota() {
        given(redisCacheAction.getIntValue(anyString())).willReturn(5);

        FormValidationEmailQuotaResponse result = formQueryService.getEmailQuota(1L);

        assertThat(result.quota()).isEqualTo(15);
    }

    @Test
    @DisplayName("getAllUnregisteredRequests - 미등록 신청 목록 반환")
    void getAllUnregisteredRequests_returnsFormListResponse() {
        FormRequest request = FormRequest.createRequest("https://link", "email@test.com", 100, 1000, 1L);
        given(formRequestRepository.findAllUnregistered()).willReturn(List.of(request));

        FormListResponse result = formQueryService.getAllUnregisteredRequests();

        assertThat(result.totalCount()).isEqualTo(1);
        assertThat(result.requests()).hasSize(1);
    }

    @Test
    @DisplayName("getAllUnregisteredRequests - 빈 목록이면 totalCount=0")
    void getAllUnregisteredRequests_empty_returnsZeroCount() {
        given(formRequestRepository.findAllUnregistered()).willReturn(List.of());

        FormListResponse result = formQueryService.getAllUnregisteredRequests();

        assertThat(result.totalCount()).isEqualTo(0);
        assertThat(result.requests()).isEmpty();
    }
}
