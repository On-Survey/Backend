package OneQ.OnSurvey.domain.discount.service;

import OneQ.OnSurvey.domain.discount.DiscountCodeErrorCode;
import OneQ.OnSurvey.domain.discount.entity.DiscountCode;
import OneQ.OnSurvey.domain.discount.model.response.DiscountCodeResponse;
import OneQ.OnSurvey.domain.discount.model.response.ValidateDiscountCodeResponse;
import OneQ.OnSurvey.domain.discount.repository.DiscountCodeRepository;
import OneQ.OnSurvey.global.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DiscountCodeQueryServiceTest {

    @Mock
    private DiscountCodeRepository discountCodeRepository;

    @InjectMocks
    private DiscountCodeQueryService discountCodeQueryService;

    private DiscountCode buildCode(String code, LocalDate expiredAt) {
        return DiscountCode.of("테스트기업", code, expiredAt);
    }

    @Test
    @DisplayName("validate - 코드 없으면 DISCOUNT_CODE_NOT_FOUND 예외")
    void validate_notFound_throwsException() {
        given(discountCodeRepository.findByCode("XXXXXX")).willReturn(Optional.empty());

        assertThatThrownBy(() -> discountCodeQueryService.validate("XXXXXX"))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(DiscountCodeErrorCode.DISCOUNT_CODE_NOT_FOUND));
    }

    @Test
    @DisplayName("validate - 만료된 코드면 DISCOUNT_CODE_EXPIRED 예외")
    void validate_expired_throwsException() {
        DiscountCode expired = buildCode("AAAAAA", LocalDate.now().minusDays(1));
        given(discountCodeRepository.findByCode("AAAAAA")).willReturn(Optional.of(expired));

        assertThatThrownBy(() -> discountCodeQueryService.validate("AAAAAA"))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(DiscountCodeErrorCode.DISCOUNT_CODE_EXPIRED));
    }

    @Test
    @DisplayName("validate - 유효한 코드면 eligible=true 반환")
    void validate_valid_returnsEligibleTrue() {
        DiscountCode valid = buildCode("BBBBBB", LocalDate.now().plusDays(30));
        given(discountCodeRepository.findByCode("BBBBBB")).willReturn(Optional.of(valid));

        ValidateDiscountCodeResponse response = discountCodeQueryService.validate("BBBBBB");

        assertThat(response.eligible()).isTrue();
    }

    @Test
    @DisplayName("getByCode - 코드 없으면 DISCOUNT_CODE_NOT_FOUND 예외")
    void getByCode_notFound_throwsException() {
        given(discountCodeRepository.findByCode("YYYYYY")).willReturn(Optional.empty());

        assertThatThrownBy(() -> discountCodeQueryService.getByCode("YYYYYY"))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(DiscountCodeErrorCode.DISCOUNT_CODE_NOT_FOUND));
    }

    @Test
    @DisplayName("getByCode - 만료된 코드면 DISCOUNT_CODE_EXPIRED 예외")
    void getByCode_expired_throwsException() {
        DiscountCode expired = buildCode("CCCCCC", LocalDate.now().minusDays(1));
        given(discountCodeRepository.findByCode("CCCCCC")).willReturn(Optional.of(expired));

        assertThatThrownBy(() -> discountCodeQueryService.getByCode("CCCCCC"))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(DiscountCodeErrorCode.DISCOUNT_CODE_EXPIRED));
    }

    @Test
    @DisplayName("getByCode - 유효한 코드면 엔티티 반환")
    void getByCode_valid_returnsEntity() {
        DiscountCode valid = buildCode("DDDDDD", LocalDate.now().plusDays(10));
        given(discountCodeRepository.findByCode("DDDDDD")).willReturn(Optional.of(valid));

        DiscountCode result = discountCodeQueryService.getByCode("DDDDDD");

        assertThat(result.getCode()).isEqualTo("DDDDDD");
        assertThat(result.getOrganizationName()).isEqualTo("테스트기업");
    }

    @Test
    @DisplayName("findAll - 활성 코드 우선, 만료 코드 후순; 각 그룹 내 만료일 오름차순(가까운 날짜 먼저)")
    void findAll_sortedActiveFirst() {
        LocalDate today = LocalDate.now();
        DiscountCode expiredEarlier = buildCode("EXP001", today.minusDays(5));
        DiscountCode expiredLater   = buildCode("EXP002", today.minusDays(1));
        DiscountCode activeNear     = buildCode("ACT002", today.plusDays(3));
        DiscountCode activeFar      = buildCode("ACT001", today.plusDays(10));
        given(discountCodeRepository.findAll()).willReturn(List.of(expiredEarlier, activeFar, expiredLater, activeNear));

        List<DiscountCodeResponse> results = discountCodeQueryService.findAll();

        assertThat(results).hasSize(4);
        assertThat(results.get(0).code()).isEqualTo("ACT002");
        assertThat(results.get(1).code()).isEqualTo("ACT001");
        assertThat(results.get(2).code()).isEqualTo("EXP001");
        assertThat(results.get(3).code()).isEqualTo("EXP002");
    }

    @Test
    @DisplayName("findAll - 만료일이 오늘인 코드는 활성으로 분류(isBefore 기준)")
    void findAll_todayExpiry_treatedAsActive() {
        LocalDate today = LocalDate.now();
        DiscountCode todayCode  = buildCode("TODAY1", today);
        DiscountCode expiredCode = buildCode("EXP001", today.minusDays(1));
        given(discountCodeRepository.findAll()).willReturn(List.of(expiredCode, todayCode));

        List<DiscountCodeResponse> results = discountCodeQueryService.findAll();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).code()).isEqualTo("TODAY1");
        assertThat(results.get(1).code()).isEqualTo("EXP001");
    }

    @Test
    @DisplayName("findAll - 빈 목록이면 빈 리스트 반환")
    void findAll_empty_returnsEmptyList() {
        given(discountCodeRepository.findAll()).willReturn(List.of());

        List<DiscountCodeResponse> results = discountCodeQueryService.findAll();

        assertThat(results).isEmpty();
    }
}
