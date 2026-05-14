package OneQ.OnSurvey.domain.survey.service.command;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.MemberErrorCode;
import OneQ.OnSurvey.domain.member.repository.MemberRepository;
import OneQ.OnSurvey.domain.member.value.MemberStatus;
import OneQ.OnSurvey.domain.member.value.Role;
import OneQ.OnSurvey.domain.question.service.QuestionQueryService;
import OneQ.OnSurvey.domain.discount.service.DiscountCodeQueryService;
import OneQ.OnSurvey.domain.survey.SurveyErrorCode;
import OneQ.OnSurvey.domain.survey.entity.Screening;
import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.entity.SurveyInfo;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.domain.survey.model.response.ScreeningResponse;
import OneQ.OnSurvey.domain.survey.repository.SurveyRepository;
import OneQ.OnSurvey.domain.survey.repository.screening.ScreeningRepository;
import OneQ.OnSurvey.domain.survey.repository.surveyInfo.SurveyInfoRepository;
import OneQ.OnSurvey.domain.survey.service.SurveyGlobalStatsService;
import OneQ.OnSurvey.domain.survey.service.refund.SurveyRefundPolicy;
import OneQ.OnSurvey.global.common.exception.CustomException;
import OneQ.OnSurvey.global.infra.discord.notifier.AlertNotifier;
import OneQ.OnSurvey.global.infra.redis.RedisAgent;
import OneQ.OnSurvey.global.infra.transaction.AfterCommitExecutor;
import OneQ.OnSurvey.global.infra.transaction.TransactionHandler;
import OneQ.OnSurvey.global.promotion.application.PromotionTierResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SurveyCommandServiceTest {

    @Mock private SurveyRepository surveyRepository;
    @Mock private ScreeningRepository screeningRepository;
    @Mock private SurveyInfoRepository surveyInfoRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private SurveyRefundPolicy surveyRefundPolicy;
    @Mock private SurveyGlobalStatsService surveyGlobalStatsService;
    @Mock private QuestionQueryService questionQueryService;
    @Mock private PromotionTierResolver promotionTierResolver;
    @Mock private DiscountCodeQueryService discountCodeQueryService;
    @Mock private AlertNotifier alertNotifier;
    @Mock private AfterCommitExecutor afterCommitExecutor;
    @Mock private RedisAgent redisAgent;
    @Mock private TransactionHandler transactionHandler;

    @InjectMocks
    private SurveyCommandService surveyCommandService;

    private Survey buildSurvey(Long id, Long memberId) {
        Survey survey = Survey.of(memberId, "테스트 설문", "설명");
        ReflectionTestUtils.setField(survey, "id", id);
        return survey;
    }

    private SurveyInfo buildRefundableSurveyInfo(Long surveyId) {
        return SurveyInfo.createSurveyInfo(
                surveyId, 100, Gender.ALL, Set.of(), Set.of(), 0, 0, 0, 0, 0, null
        );
    }

    private Member buildMember(Long userKey) {
        Member member = Member.createMember(
                userKey, "홍길동", "010-1234-5678",
                "19900101", "test@test.com",
                Gender.MALE, Role.ROLE_MEMBER, MemberStatus.ACTIVE
        );
        member.increaseCoin(1000L);
        return member;
    }

    @Test
    @DisplayName("upsertScreening - content가 null이면 기존 스크리닝 삭제")
    void upsertScreening_nullContent_deletesExisting() {
        Screening existing = Screening.of(1L, "기존 질문", true);
        given(screeningRepository.getScreeningBySurveyId(1L)).willReturn(existing);

        ScreeningResponse response = surveyCommandService.upsertScreening(1L, null, null);

        verify(screeningRepository).delete(existing);
        assertThat(response.screeningId()).isNull();
    }

    @Test
    @DisplayName("upsertScreening - content가 blank이면 기존 스크리닝 삭제")
    void upsertScreening_blankContent_deletesExisting() {
        Screening existing = Screening.of(1L, "기존 질문", true);
        given(screeningRepository.getScreeningBySurveyId(1L)).willReturn(existing);

        ScreeningResponse response = surveyCommandService.upsertScreening(1L, "   ", true);

        verify(screeningRepository).delete(existing);
        assertThat(response.screeningId()).isNull();
    }

    @Test
    @DisplayName("upsertScreening - 스크리닝 없고 유효한 값이면 신규 생성")
    void upsertScreening_noExisting_createsNew() {
        Screening created = Screening.of(1L, "새 질문", false);
        ReflectionTestUtils.setField(created, "id", 99L);
        given(screeningRepository.getScreeningBySurveyId(1L)).willReturn(null);
        given(screeningRepository.save(any(Screening.class))).willReturn(created);

        ScreeningResponse response = surveyCommandService.upsertScreening(1L, "새 질문", false);

        assertThat(response.content()).isEqualTo("새 질문");
        assertThat(response.answer()).isFalse();
    }

    @Test
    @DisplayName("upsertScreening - 기존 스크리닝 있으면 내용 업데이트")
    void upsertScreening_existingScreening_updates() {
        Screening existing = Screening.of(1L, "기존 질문", true);
        ReflectionTestUtils.setField(existing, "id", 5L);
        given(screeningRepository.getScreeningBySurveyId(1L)).willReturn(existing);
        given(screeningRepository.save(existing)).willReturn(existing);

        ScreeningResponse response = surveyCommandService.upsertScreening(1L, "수정된 질문", false);

        assertThat(response.content()).isEqualTo("수정된 질문");
        assertThat(response.answer()).isFalse();
    }

    @Test
    @DisplayName("refundSurvey - 설문 없으면 SURVEY_NOT_FOUND 예외")
    void refundSurvey_surveyNotFound_throwsException() {
        given(surveyRepository.getSurveyById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> surveyCommandService.refundSurvey(1L, 999L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(SurveyErrorCode.SURVEY_NOT_FOUND));
    }

    @Test
    @DisplayName("refundSurvey - surveyInfo 없으면 SURVEY_INFO_NOT_FOUND 예외")
    void refundSurvey_infoNotFound_throwsException() {
        Survey survey = buildSurvey(1L, 10L);
        given(surveyRepository.getSurveyById(1L)).willReturn(Optional.of(survey));
        given(surveyInfoRepository.findBySurveyId(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> surveyCommandService.refundSurvey(1L, 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(SurveyErrorCode.SURVEY_INFO_NOT_FOUND));
    }

    @Test
    @DisplayName("refundSurvey - 환불 불가 상태면 SURVEY_NOT_REFUNDABLE 예외")
    void refundSurvey_notRefundable_throwsException() {
        Survey survey = buildSurvey(1L, 10L);
        SurveyInfo info = buildRefundableSurveyInfo(1L);
        info.markNonRefundable();
        given(surveyRepository.getSurveyById(1L)).willReturn(Optional.of(survey));
        given(surveyInfoRepository.findBySurveyId(1L)).willReturn(Optional.of(info));

        assertThatThrownBy(() -> surveyCommandService.refundSurvey(1L, 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(SurveyErrorCode.SURVEY_NOT_REFUNDABLE));
    }

    @Test
    @DisplayName("refundSurvey - 환불 금액이 0 이하면 SURVEY_NOT_REFUNDABLE 예외")
    void refundSurvey_zeroRefundAmount_throwsException() {
        Survey survey = buildSurvey(1L, 10L);
        SurveyInfo info = buildRefundableSurveyInfo(1L);
        given(surveyRepository.getSurveyById(1L)).willReturn(Optional.of(survey));
        given(surveyInfoRepository.findBySurveyId(1L)).willReturn(Optional.of(info));
        given(surveyRefundPolicy.calculateRefundAmount(survey, info)).willReturn(0);

        assertThatThrownBy(() -> surveyCommandService.refundSurvey(1L, 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(SurveyErrorCode.SURVEY_NOT_REFUNDABLE));
    }

    @Test
    @DisplayName("refundSurvey - 환불 성공 시 코인 지급 및 설문 상태 REFUNDED")
    void refundSurvey_success_increasesCoinAndUpdatesStatus() {
        Survey survey = buildSurvey(1L, 10L);
        SurveyInfo info = buildRefundableSurveyInfo(1L);
        Member member = buildMember(1L);
        given(surveyRepository.getSurveyById(1L)).willReturn(Optional.of(survey));
        given(surveyInfoRepository.findBySurveyId(1L)).willReturn(Optional.of(info));
        given(surveyRefundPolicy.calculateRefundAmount(survey, info)).willReturn(500);
        given(memberRepository.findMemberByUserKey(1L)).willReturn(Optional.of(member));

        Boolean result = surveyCommandService.refundSurvey(1L, 1L);

        assertThat(result).isTrue();
        assertThat(member.getCoin()).isEqualTo(1500L);
        assertThat(survey.getStatus()).isEqualTo(SurveyStatus.REFUNDED);
    }

    @Test
    @DisplayName("refundSurvey - 멤버 없으면 MEMBER_NOT_FOUND 예외")
    void refundSurvey_memberNotFound_throwsException() {
        Survey survey = buildSurvey(1L, 10L);
        SurveyInfo info = buildRefundableSurveyInfo(1L);
        given(surveyRepository.getSurveyById(1L)).willReturn(Optional.of(survey));
        given(surveyInfoRepository.findBySurveyId(1L)).willReturn(Optional.of(info));
        given(surveyRefundPolicy.calculateRefundAmount(survey, info)).willReturn(300);
        given(memberRepository.findMemberByUserKey(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> surveyCommandService.refundSurvey(1L, 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}
