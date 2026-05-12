package OneQ.OnSurvey.domain.survey.service.export;

import OneQ.OnSurvey.domain.survey.SurveyErrorCode;
import OneQ.OnSurvey.domain.survey.entity.SurveyInfo;
import OneQ.OnSurvey.domain.survey.model.AgeRange;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.Residence;
import OneQ.OnSurvey.domain.survey.model.export.SurveyAnswerProjection;
import OneQ.OnSurvey.domain.survey.model.export.SurveyExportFile;
import OneQ.OnSurvey.domain.survey.model.export.SurveyMemberProjection;
import OneQ.OnSurvey.domain.survey.model.export.SurveyQuestionHeader;
import OneQ.OnSurvey.domain.survey.repository.export.SurveyExportRepository;
import OneQ.OnSurvey.domain.survey.repository.surveyInfo.SurveyInfoRepository;
import OneQ.OnSurvey.global.common.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SurveyExportServiceTest {

    @Mock
    private SurveyExportRepository surveyExportRepository;

    @Mock
    private SurveyInfoRepository surveyInfoRepository;

    @InjectMocks
    private SurveyExportService surveyExportService;

    private static final Long SURVEY_ID = 1L;
    private static final Long MEMBER_ID = 10L;

    @BeforeEach
    void setUp() {
    }

    private SurveyInfo buildSurveyInfo(Gender gender, Set<AgeRange> ages) {
        return SurveyInfo.builder()
                .surveyId(SURVEY_ID)
                .dueCount(100)
                .completedCount(0)
                .gender(gender)
                .ages(ages)
                .residences(Set.of(Residence.ALL))
                .genderPrice(0)
                .agePrice(0)
                .residencePrice(0)
                .dueCountPrice(0)
                .promotionAmount(0)
                .build();
    }

    @Test
    @DisplayName("소유자가 아니면 SURVEY_FORBIDDEN 예외 발생")
    void exportCsv_notOwner_throwsForbidden() {
        given(surveyExportRepository.existsOwnedSurvey(SURVEY_ID, MEMBER_ID)).willReturn(false);

        assertThatThrownBy(() -> surveyExportService.exportCsv(SURVEY_ID, MEMBER_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(SurveyErrorCode.SURVEY_FORBIDDEN));
    }

    @Test
    @DisplayName("SurveyInfo가 없으면 SURVEY_INFO_NOT_FOUND 예외 발생")
    void exportCsv_surveyInfoNotFound_throwsException() {
        given(surveyExportRepository.existsOwnedSurvey(SURVEY_ID, MEMBER_ID)).willReturn(true);
        given(surveyInfoRepository.findBySurveyId(SURVEY_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> surveyExportService.exportCsv(SURVEY_ID, MEMBER_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(SurveyErrorCode.SURVEY_INFO_NOT_FOUND));
    }

    @Test
    @DisplayName("gender=ALL이면 gender 컬럼 미포함")
    void exportCsv_genderAll_noGenderColumn() {
        SurveyInfo info = buildSurveyInfo(Gender.ALL, Set.of(AgeRange.ALL));
        given(surveyExportRepository.existsOwnedSurvey(SURVEY_ID, MEMBER_ID)).willReturn(true);
        given(surveyInfoRepository.findBySurveyId(SURVEY_ID)).willReturn(Optional.of(info));
        given(surveyExportRepository.findQuestionHeaders(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findMembersWhoAnswered(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findAnswers(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findSurveyTitle(SURVEY_ID)).willReturn("테스트설문");

        SurveyExportFile file = surveyExportService.exportCsv(SURVEY_ID, MEMBER_ID);

        String csv = removeBom(file.bytes());
        assertThat(csv).doesNotContain("gender");
    }

    @Test
    @DisplayName("gender=MALE이면 gender 컬럼 포함")
    void exportCsv_specificGender_hasGenderColumn() {
        SurveyInfo info = buildSurveyInfo(Gender.MALE, Set.of(AgeRange.ALL));
        given(surveyExportRepository.existsOwnedSurvey(SURVEY_ID, MEMBER_ID)).willReturn(true);
        given(surveyInfoRepository.findBySurveyId(SURVEY_ID)).willReturn(Optional.of(info));
        given(surveyExportRepository.findQuestionHeaders(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findMembersWhoAnswered(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findAnswers(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findSurveyTitle(SURVEY_ID)).willReturn("테스트설문");

        SurveyExportFile file = surveyExportService.exportCsv(SURVEY_ID, MEMBER_ID);

        String csv = removeBom(file.bytes());
        assertThat(csv).contains("gender");
    }

    @Test
    @DisplayName("ages=ALL이면 age 컬럼 미포함")
    void exportCsv_ageAll_noAgeColumn() {
        SurveyInfo info = buildSurveyInfo(Gender.ALL, Set.of(AgeRange.ALL));
        given(surveyExportRepository.existsOwnedSurvey(SURVEY_ID, MEMBER_ID)).willReturn(true);
        given(surveyInfoRepository.findBySurveyId(SURVEY_ID)).willReturn(Optional.of(info));
        given(surveyExportRepository.findQuestionHeaders(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findMembersWhoAnswered(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findAnswers(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findSurveyTitle(SURVEY_ID)).willReturn("설문");

        SurveyExportFile file = surveyExportService.exportCsv(SURVEY_ID, MEMBER_ID);

        String csv = removeBom(file.bytes());
        assertThat(csv).doesNotContain("age");
    }

    @Test
    @DisplayName("특정 연령대 설정 시 age 컬럼 포함")
    void exportCsv_specificAge_hasAgeColumn() {
        SurveyInfo info = buildSurveyInfo(Gender.ALL, Set.of(AgeRange.TWENTY));
        given(surveyExportRepository.existsOwnedSurvey(SURVEY_ID, MEMBER_ID)).willReturn(true);
        given(surveyInfoRepository.findBySurveyId(SURVEY_ID)).willReturn(Optional.of(info));
        given(surveyExportRepository.findQuestionHeaders(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findMembersWhoAnswered(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findAnswers(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findSurveyTitle(SURVEY_ID)).willReturn("설문");

        SurveyExportFile file = surveyExportService.exportCsv(SURVEY_ID, MEMBER_ID);

        String csv = removeBom(file.bytes());
        assertThat(csv).contains("age");
    }

    @Test
    @DisplayName("질문 헤더와 응답자 데이터가 올바르게 CSV에 포함됨")
    void exportCsv_withQuestionsAndMembers_csvContainsData() {
        SurveyInfo info = buildSurveyInfo(Gender.ALL, Set.of(AgeRange.ALL));
        List<SurveyQuestionHeader> headers = List.of(
                new SurveyQuestionHeader(100L, 0, "좋아하는 음식", 0, null)
        );
        List<SurveyMemberProjection> members = List.of(
                new SurveyMemberProjection(MEMBER_ID, "19900101", "MALE", "서울")
        );
        List<SurveyAnswerProjection> answers = List.of(
                new SurveyAnswerProjection(MEMBER_ID, 100L, "피자", 0)
        );

        given(surveyExportRepository.existsOwnedSurvey(SURVEY_ID, MEMBER_ID)).willReturn(true);
        given(surveyInfoRepository.findBySurveyId(SURVEY_ID)).willReturn(Optional.of(info));
        given(surveyExportRepository.findQuestionHeaders(SURVEY_ID)).willReturn(headers);
        given(surveyExportRepository.findMembersWhoAnswered(SURVEY_ID)).willReturn(members);
        given(surveyExportRepository.findAnswers(SURVEY_ID)).willReturn(answers);
        given(surveyExportRepository.findSurveyTitle(SURVEY_ID)).willReturn("음식설문");

        SurveyExportFile file = surveyExportService.exportCsv(SURVEY_ID, MEMBER_ID);

        String csv = removeBom(file.bytes());
        assertThat(csv).contains("Q1. 좋아하는 음식");
        assertThat(csv).contains("서울");
        assertThat(csv).contains("피자");
    }

    @Test
    @DisplayName("CSV 파일에 BOM(UTF-8)이 포함됨")
    void exportCsv_hasBom() {
        SurveyInfo info = buildSurveyInfo(Gender.ALL, Set.of(AgeRange.ALL));
        given(surveyExportRepository.existsOwnedSurvey(SURVEY_ID, MEMBER_ID)).willReturn(true);
        given(surveyInfoRepository.findBySurveyId(SURVEY_ID)).willReturn(Optional.of(info));
        given(surveyExportRepository.findQuestionHeaders(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findMembersWhoAnswered(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findAnswers(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findSurveyTitle(SURVEY_ID)).willReturn("설문");

        SurveyExportFile file = surveyExportService.exportCsv(SURVEY_ID, MEMBER_ID);

        assertThat(file.bytes()[0]).isEqualTo((byte) 0xEF);
        assertThat(file.bytes()[1]).isEqualTo((byte) 0xBB);
        assertThat(file.bytes()[2]).isEqualTo((byte) 0xBF);
    }

    @Test
    @DisplayName("파일명에 날짜가 포함되고 확장자가 .csv")
    void exportCsv_filenameHasDateAndCsvExtension() {
        SurveyInfo info = buildSurveyInfo(Gender.ALL, Set.of(AgeRange.ALL));
        given(surveyExportRepository.existsOwnedSurvey(SURVEY_ID, MEMBER_ID)).willReturn(true);
        given(surveyInfoRepository.findBySurveyId(SURVEY_ID)).willReturn(Optional.of(info));
        given(surveyExportRepository.findQuestionHeaders(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findMembersWhoAnswered(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findAnswers(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findSurveyTitle(SURVEY_ID)).willReturn("음식 설문");

        SurveyExportFile file = surveyExportService.exportCsv(SURVEY_ID, MEMBER_ID);

        assertThat(file.filename()).endsWith(".csv");
        assertThat(file.filename()).startsWith("음식 설문_");
        assertThat(file.contentType()).isEqualTo("text/csv; charset=UTF-8");
    }

    @Test
    @DisplayName("파일명에 특수문자가 있으면 공백으로 치환")
    void exportCsv_titleWithSpecialChars_sanitized() {
        SurveyInfo info = buildSurveyInfo(Gender.ALL, Set.of(AgeRange.ALL));
        given(surveyExportRepository.existsOwnedSurvey(SURVEY_ID, MEMBER_ID)).willReturn(true);
        given(surveyInfoRepository.findBySurveyId(SURVEY_ID)).willReturn(Optional.of(info));
        given(surveyExportRepository.findQuestionHeaders(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findMembersWhoAnswered(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findAnswers(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findSurveyTitle(SURVEY_ID)).willReturn("설문/테스트:조사*파일");

        SurveyExportFile file = surveyExportService.exportCsv(SURVEY_ID, MEMBER_ID);

        assertThat(file.filename()).doesNotContain("/", ":", "*");
    }

    @Test
    @DisplayName("surveyTitle이 null이면 파일명 기본값 'survey'")
    void exportCsv_nullTitle_defaultFilename() {
        SurveyInfo info = buildSurveyInfo(Gender.ALL, Set.of(AgeRange.ALL));
        given(surveyExportRepository.existsOwnedSurvey(SURVEY_ID, MEMBER_ID)).willReturn(true);
        given(surveyInfoRepository.findBySurveyId(SURVEY_ID)).willReturn(Optional.of(info));
        given(surveyExportRepository.findQuestionHeaders(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findMembersWhoAnswered(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findAnswers(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findSurveyTitle(SURVEY_ID)).willReturn(null);

        SurveyExportFile file = surveyExportService.exportCsv(SURVEY_ID, MEMBER_ID);

        assertThat(file.filename()).startsWith("survey_");
    }

    @Test
    @DisplayName("콤마 포함 응답은 큰따옴표로 감싸서 CSV 이스케이프")
    void exportCsv_answerWithComma_escapedWithQuotes() {
        SurveyInfo info = buildSurveyInfo(Gender.ALL, Set.of(AgeRange.ALL));
        List<SurveyQuestionHeader> headers = List.of(
                new SurveyQuestionHeader(100L, 0, "음식", 0, null)
        );
        List<SurveyMemberProjection> members = List.of(
                new SurveyMemberProjection(MEMBER_ID, null, null, "서울")
        );
        List<SurveyAnswerProjection> answers = List.of(
                new SurveyAnswerProjection(MEMBER_ID, 100L, "피자,파스타", 0)
        );

        given(surveyExportRepository.existsOwnedSurvey(SURVEY_ID, MEMBER_ID)).willReturn(true);
        given(surveyInfoRepository.findBySurveyId(SURVEY_ID)).willReturn(Optional.of(info));
        given(surveyExportRepository.findQuestionHeaders(SURVEY_ID)).willReturn(headers);
        given(surveyExportRepository.findMembersWhoAnswered(SURVEY_ID)).willReturn(members);
        given(surveyExportRepository.findAnswers(SURVEY_ID)).willReturn(answers);
        given(surveyExportRepository.findSurveyTitle(SURVEY_ID)).willReturn("설문");

        SurveyExportFile file = surveyExportService.exportCsv(SURVEY_ID, MEMBER_ID);

        String csv = removeBom(file.bytes());
        assertThat(csv).contains("\"피자,파스타\"");
    }

    @Test
    @DisplayName("exportCsvForAdmin은 소유자 체크 없이 CSV 생성")
    void exportCsvForAdmin_noOwnerCheck() {
        SurveyInfo info = buildSurveyInfo(Gender.ALL, Set.of(AgeRange.ALL));
        given(surveyInfoRepository.findBySurveyId(SURVEY_ID)).willReturn(Optional.of(info));
        given(surveyExportRepository.findQuestionHeaders(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findMembersWhoAnswered(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findAnswers(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findSurveyTitle(SURVEY_ID)).willReturn("관리자용설문");

        SurveyExportFile file = surveyExportService.exportCsvForAdmin(SURVEY_ID);

        assertThat(file).isNotNull();
        assertThat(file.filename()).contains("관리자용설문");
    }

    @Test
    @DisplayName("응답자의 생년월일로 나이 계산")
    void exportCsv_withBirthday_ageCalculated() {
        SurveyInfo info = buildSurveyInfo(Gender.ALL, Set.of(AgeRange.TWENTY));
        given(surveyExportRepository.existsOwnedSurvey(SURVEY_ID, MEMBER_ID)).willReturn(true);
        given(surveyInfoRepository.findBySurveyId(SURVEY_ID)).willReturn(Optional.of(info));
        given(surveyExportRepository.findQuestionHeaders(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findMembersWhoAnswered(SURVEY_ID)).willReturn(
                List.of(new SurveyMemberProjection(MEMBER_ID, "2000-01-01", null, "서울"))
        );
        given(surveyExportRepository.findAnswers(SURVEY_ID)).willReturn(List.of());
        given(surveyExportRepository.findSurveyTitle(SURVEY_ID)).willReturn("설문");

        SurveyExportFile file = surveyExportService.exportCsv(SURVEY_ID, MEMBER_ID);

        String csv = removeBom(file.bytes());
        // 2000년생이면 2026 - 2000 + 1 = 27
        assertThat(csv).contains("27");
    }

    private String removeBom(byte[] bytes) {
        if (bytes.length >= 3
                && bytes[0] == (byte) 0xEF
                && bytes[1] == (byte) 0xBB
                && bytes[2] == (byte) 0xBF) {
            return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
