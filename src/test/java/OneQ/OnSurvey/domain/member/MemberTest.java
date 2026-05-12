package OneQ.OnSurvey.domain.member;

import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.member.value.MemberStatus;
import OneQ.OnSurvey.domain.member.value.Role;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.Residence;
import OneQ.OnSurvey.global.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberTest {

    private Member buildMember() {
        return Member.createMember(
                1000L, "홍길동", "010-1234-5678",
                "19900101", "test@test.com",
                Gender.MALE, Role.ROLE_MEMBER, MemberStatus.ACTIVE
        );
    }

    @Test
    @DisplayName("increaseCoin - 양수 금액이면 코인 증가")
    void increaseCoin_positiveAmount_increasesCoin() {
        Member member = buildMember();

        member.increaseCoin(500L);

        assertThat(member.getCoin()).isEqualTo(500L);
    }

    @Test
    @DisplayName("increaseCoin - 0 이하이면 COIN_NOT_POSITIVE 예외")
    void increaseCoin_zero_throwsException() {
        Member member = buildMember();

        assertThatThrownBy(() -> member.increaseCoin(0L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(CoinErrorCode.COIN_NOT_POSITIVE));
    }

    @Test
    @DisplayName("increaseCoin - 음수이면 COIN_NOT_POSITIVE 예외")
    void increaseCoin_negative_throwsException() {
        Member member = buildMember();

        assertThatThrownBy(() -> member.increaseCoin(-100L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(CoinErrorCode.COIN_NOT_POSITIVE));
    }

    @Test
    @DisplayName("decreaseCoin - 충분한 코인이면 차감")
    void decreaseCoin_sufficientCoin_decreasesCoin() {
        Member member = buildMember();
        member.increaseCoin(1000L);

        member.decreaseCoin(300L);

        assertThat(member.getCoin()).isEqualTo(700L);
    }

    @Test
    @DisplayName("decreaseCoin - 코인 부족이면 COIN_LACK 예외")
    void decreaseCoin_insufficientCoin_throwsException() {
        Member member = buildMember();
        member.increaseCoin(100L);

        assertThatThrownBy(() -> member.decreaseCoin(500L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(CoinErrorCode.COIN_LACK));
    }

    @Test
    @DisplayName("decreaseCoin - 0 이하이면 COIN_NOT_POSITIVE 예외")
    void decreaseCoin_zero_throwsException() {
        Member member = buildMember();

        assertThatThrownBy(() -> member.decreaseCoin(0L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(CoinErrorCode.COIN_NOT_POSITIVE));
    }

    @Test
    @DisplayName("decreaseCoin - 음수이면 COIN_NOT_POSITIVE 예외")
    void decreaseCoin_negative_throwsException() {
        Member member = buildMember();

        assertThatThrownBy(() -> member.decreaseCoin(-1L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(CoinErrorCode.COIN_NOT_POSITIVE));
    }

    @Test
    @DisplayName("increasePromotionPoint - 양수 금액이면 포인트 증가")
    void increasePromotionPoint_positiveAmount_increasesPoint() {
        Member member = buildMember();

        member.increasePromotionPoint(200L);

        assertThat(member.getPromotionPoint()).isEqualTo(200L);
    }

    @Test
    @DisplayName("increasePromotionPoint - 0 이하이면 COIN_NOT_POSITIVE 예외")
    void increasePromotionPoint_zero_throwsException() {
        Member member = buildMember();

        assertThatThrownBy(() -> member.increasePromotionPoint(0L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(CoinErrorCode.COIN_NOT_POSITIVE));
    }

    @Test
    @DisplayName("increasePromotionPoint - 음수이면 COIN_NOT_POSITIVE 예외")
    void increasePromotionPoint_negative_throwsException() {
        Member member = buildMember();

        assertThatThrownBy(() -> member.increasePromotionPoint(-1L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(CoinErrorCode.COIN_NOT_POSITIVE));
    }

    @Test
    @DisplayName("update - null 값은 무시하고 비null 값만 업데이트")
    void update_nullFieldsIgnored() {
        Member member = buildMember();

        member.update(null, "010-9999-9999", null, null, null, MemberStatus.ACTIVE);

        assertThat(member.getName()).isEqualTo("홍길동");
        assertThat(member.getPhoneNumber()).isEqualTo("010-9999-9999");
        assertThat(member.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("updateAgreePolicy - serviceAgreed 항목 포함 시 서비스 동의 true")
    void updateAgreePolicy_withServiceAgreed_setsTrue() {
        Member member = buildMember();

        member.updateAgreePolicy(List.of("serviceAgreed"));

        assertThat(member.isServiceAgreed()).isTrue();
        assertThat(member.isMarketingAgreed()).isFalse();
    }

    @Test
    @DisplayName("updateAgreePolicy - marketingAgreed 항목 포함 시 마케팅 동의 true")
    void updateAgreePolicy_withMarketingAgreed_setsTrue() {
        Member member = buildMember();

        member.updateAgreePolicy(List.of("marketingAgreed"));

        assertThat(member.isMarketingAgreed()).isTrue();
        assertThat(member.isServiceAgreed()).isFalse();
    }

    @Test
    @DisplayName("updateAgreePolicy - null 이면 동의 상태 변경 없음")
    void updateAgreePolicy_null_noChange() {
        Member member = buildMember();

        member.updateAgreePolicy(null);

        assertThat(member.isServiceAgreed()).isFalse();
        assertThat(member.isMarketingAgreed()).isFalse();
    }

    @Test
    @DisplayName("memberConnectOut - 상태 TOSS_CONNECT_OUT으로 변경")
    void memberConnectOut_changesStatus() {
        Member member = buildMember();

        member.memberConnectOut();

        assertThat(member.getStatus()).isEqualTo(MemberStatus.TOSS_CONNECT_OUT);
    }

    @Test
    @DisplayName("changeProfileUrl - 프로필 URL 변경")
    void changeProfileUrl_updatesUrl() {
        Member member = buildMember();

        member.changeProfileUrl("https://cdn.example.com/avatar.png");

        assertThat(member.getProfileUrl()).isEqualTo("https://cdn.example.com/avatar.png");
    }

    @Test
    @DisplayName("completeOnboarding - 거주지, 관심사 설정 및 완료 플래그 true")
    void completeOnboarding_setsResidenceAndInterests() {
        Member member = buildMember();

        member.completeOnboarding(Residence.BUSAN, Set.of(Interest.CAREER, Interest.FINANCE));

        assertThat(member.isOnboardingCompleted()).isTrue();
        assertThat(member.getResidence()).isEqualTo(Residence.BUSAN);
        assertThat(member.getInterests()).containsExactlyInAnyOrder(Interest.CAREER, Interest.FINANCE);
    }

    @Test
    @DisplayName("completeOnboarding - interests가 null이면 빈 Set으로 처리")
    void completeOnboarding_nullInterests_usesEmptySet() {
        Member member = buildMember();

        member.completeOnboarding(Residence.SEOUL, null);

        assertThat(member.isOnboardingCompleted()).isTrue();
        assertThat(member.getInterests()).isEmpty();
    }
}
