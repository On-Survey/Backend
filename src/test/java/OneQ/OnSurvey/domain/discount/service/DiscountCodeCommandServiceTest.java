package OneQ.OnSurvey.domain.discount.service;

import OneQ.OnSurvey.domain.discount.entity.DiscountCode;
import OneQ.OnSurvey.domain.discount.model.request.CreateDiscountCodeRequest;
import OneQ.OnSurvey.domain.discount.model.response.DiscountCodeResponse;
import OneQ.OnSurvey.domain.discount.repository.DiscountCodeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DiscountCodeCommandServiceTest {

    private static final Pattern CODE_PATTERN = Pattern.compile("[A-Z0-9]{6}");

    @Mock
    private DiscountCodeRepository discountCodeRepository;

    @InjectMocks
    private DiscountCodeCommandService discountCodeCommandService;

    @Test
    @DisplayName("할인 코드 생성 성공 - 6자리 대문자+숫자 코드 반환")
    void create_success() {
        CreateDiscountCodeRequest request = new CreateDiscountCodeRequest(
                "OnSurvey", LocalDate.of(2026, 12, 31)
        );
        given(discountCodeRepository.existsByCode(anyString())).willReturn(false);
        given(discountCodeRepository.save(any(DiscountCode.class)))
                .willAnswer(inv -> inv.getArgument(0));

        DiscountCodeResponse response = discountCodeCommandService.create(request);

        assertThat(response.organizationName()).isEqualTo("OnSurvey");
        assertThat(response.code()).hasSize(6);
        assertThat(CODE_PATTERN.matcher(response.code()).matches()).isTrue();
        assertThat(response.expiredAt()).isEqualTo(LocalDate.of(2026, 12, 31));
    }

    @Test
    @DisplayName("코드 중복 시 재생성 후 저장")
    void create_codeConflict_retries() {
        CreateDiscountCodeRequest request = new CreateDiscountCodeRequest(
                "TestOrg", LocalDate.of(2027, 1, 1)
        );
        // 처음 두 번은 중복, 세 번째에 성공
        given(discountCodeRepository.existsByCode(anyString()))
                .willReturn(true, true, false);
        given(discountCodeRepository.save(any(DiscountCode.class)))
                .willAnswer(inv -> inv.getArgument(0));

        DiscountCodeResponse response = discountCodeCommandService.create(request);

        assertThat(response.code()).hasSize(6);
        verify(discountCodeRepository, times(3)).existsByCode(anyString());
    }

    @Test
    @DisplayName("저장된 엔티티의 조직명이 요청값과 동일")
    void create_savedEntity_hasCorrectOrgName() {
        String orgName = "학회명";
        LocalDate expiredAt = LocalDate.of(2026, 6, 30);
        CreateDiscountCodeRequest request = new CreateDiscountCodeRequest(orgName, expiredAt);

        given(discountCodeRepository.existsByCode(anyString())).willReturn(false);
        ArgumentCaptor<DiscountCode> captor = ArgumentCaptor.forClass(DiscountCode.class);
        given(discountCodeRepository.save(captor.capture()))
                .willAnswer(inv -> inv.getArgument(0));

        discountCodeCommandService.create(request);

        DiscountCode saved = captor.getValue();
        assertThat(saved.getOrganizationName()).isEqualTo(orgName);
        assertThat(saved.getExpiredAt()).isEqualTo(expiredAt);
        assertThat(CODE_PATTERN.matcher(saved.getCode()).matches()).isTrue();
    }

}
