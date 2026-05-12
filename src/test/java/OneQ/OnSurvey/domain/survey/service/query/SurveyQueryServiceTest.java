package OneQ.OnSurvey.domain.survey.service.query;

import OneQ.OnSurvey.domain.member.repository.MemberRepository;
import OneQ.OnSurvey.domain.participation.repository.response.ResponseRepository;
import OneQ.OnSurvey.domain.question.repository.section.SectionRepository;
import OneQ.OnSurvey.domain.question.service.QuestionQueryService;
import OneQ.OnSurvey.domain.survey.SurveyErrorCode;
import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.entity.SurveyInfo;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.domain.survey.model.response.MySurveyListResponse;
import OneQ.OnSurvey.domain.survey.repository.SurveyRepository;
import OneQ.OnSurvey.domain.survey.repository.screening.ScreeningRepository;
import OneQ.OnSurvey.domain.survey.repository.surveyInfo.SurveyInfoRepository;
import OneQ.OnSurvey.global.common.exception.CustomException;
import OneQ.OnSurvey.global.infra.redis.RedisAgent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SurveyQueryServiceTest {

    @Mock private SurveyRepository surveyRepository;
    @Mock private SurveyInfoRepository surveyInfoRepository;
    @Mock private ScreeningRepository screeningRepository;
    @Mock private ResponseRepository responseRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private SectionRepository sectionRepository;
    @Mock private RedisAgent redisAgent;
    @Mock private QuestionQueryService questionQueryService;

    @InjectMocks
    private SurveyQueryService surveyQueryService;

    private Survey buildSurvey(Long id, Long memberId, SurveyStatus status, LocalDateTime createdAt) {
        Survey survey = Survey.builder()
                .memberId(memberId)
                .title("테스트 설문")
                .totalCoin(1000)
                .deadline(LocalDateTime.now().plusDays(7))
                .build();
        ReflectionTestUtils.setField(survey, "id", id);
        ReflectionTestUtils.setField(survey, "status", status);
        ReflectionTestUtils.setField(survey, "createdAt", createdAt);
        return survey;
    }

    @Test
    @DisplayName("getSurveyById - 설문 존재 시 반환")
    void getSurveyById_found_returnsSurvey() {
        Survey survey = buildSurvey(1L, 10L, SurveyStatus.ONGOING, LocalDateTime.now());
        given(surveyRepository.getSurveyById(1L)).willReturn(Optional.of(survey));

        Survey result = surveyQueryService.getSurveyById(1L);

        assertThat(result).isEqualTo(survey);
    }

    @Test
    @DisplayName("getSurveyById - 설문 없으면 SURVEY_NOT_FOUND 예외")
    void getSurveyById_notFound_throwsException() {
        given(surveyRepository.getSurveyById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> surveyQueryService.getSurveyById(999L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(SurveyErrorCode.SURVEY_NOT_FOUND));
    }

    @Test
    @DisplayName("getPromotionAmountBySurveyId - surveyInfo 존재 시 promotionAmount 반환")
    void getPromotionAmountBySurveyId_found_returnsAmount() {
        SurveyInfo info = SurveyInfo.createSurveyInfo(
                1L, 100, Gender.ALL, Set.of(), Set.of(), 0, 0, 0, 0, 300, null);
        given(surveyInfoRepository.findBySurveyId(1L)).willReturn(Optional.of(info));

        Integer result = surveyQueryService.getPromotionAmountBySurveyId(1L);

        assertThat(result).isEqualTo(300);
    }

    @Test
    @DisplayName("getPromotionAmountBySurveyId - surveyInfo 없으면 null 반환")
    void getPromotionAmountBySurveyId_notFound_returnsNull() {
        given(surveyInfoRepository.findBySurveyId(999L)).willReturn(Optional.empty());

        Integer result = surveyQueryService.getPromotionAmountBySurveyId(999L);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getMySurveys - ONGOING/CLOSED는 ongoing, REFUNDED는 refunded로 분리")
    void getMySurveys_separatesOngoingAndRefunded() {
        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 0, 0);
        Survey ongoing = buildSurvey(1L, 10L, SurveyStatus.ONGOING, base.plusDays(2));
        Survey closed  = buildSurvey(2L, 10L, SurveyStatus.CLOSED,  base.plusDays(1));
        Survey refunded = buildSurvey(3L, 10L, SurveyStatus.REFUNDED, base);
        given(surveyRepository.getSurveyListByMemberId(10L)).willReturn(List.of(ongoing, closed, refunded));

        MySurveyListResponse response = surveyQueryService.getMySurveys(10L);

        assertThat(response.totalCount()).isEqualTo(2);
        assertThat(response.refundedCount()).isEqualTo(1);
        assertThat(response.ongoingSurveys()).hasSize(2);
        assertThat(response.refundedSurveys()).hasSize(1);
    }

    @Test
    @DisplayName("getMySurveys - ongoing 목록은 createdAt 내림차순 정렬")
    void getMySurveys_ongoingSortedByCreatedAtDesc() {
        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 0, 0);
        Survey older = buildSurvey(1L, 10L, SurveyStatus.ONGOING, base);
        Survey newer = buildSurvey(2L, 10L, SurveyStatus.ONGOING, base.plusDays(5));
        given(surveyRepository.getSurveyListByMemberId(10L)).willReturn(List.of(older, newer));

        MySurveyListResponse response = surveyQueryService.getMySurveys(10L);

        assertThat(response.ongoingSurveys().get(0).surveyId()).isEqualTo(2L);
        assertThat(response.ongoingSurveys().get(1).surveyId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getMySurveys - 빈 목록이면 카운트 0")
    void getMySurveys_empty_returnsZeroCounts() {
        given(surveyRepository.getSurveyListByMemberId(10L)).willReturn(List.of());

        MySurveyListResponse response = surveyQueryService.getMySurveys(10L);

        assertThat(response.totalCount()).isZero();
        assertThat(response.refundedCount()).isZero();
    }
}
