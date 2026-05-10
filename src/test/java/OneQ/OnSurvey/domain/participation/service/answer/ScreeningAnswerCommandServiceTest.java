package OneQ.OnSurvey.domain.participation.service.answer;

import OneQ.OnSurvey.domain.participation.entity.Response;
import OneQ.OnSurvey.domain.participation.entity.ScreeningAnswer;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;
import OneQ.OnSurvey.domain.participation.repository.answer.AnswerRepository;
import OneQ.OnSurvey.domain.participation.repository.response.ResponseRepository;
import OneQ.OnSurvey.domain.survey.repository.screening.ScreeningRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScreeningAnswerCommandServiceTest {

    @Mock private AnswerRepository<ScreeningAnswer> answerRepository;
    @Mock private ResponseRepository responseRepository;
    @Mock private ScreeningRepository screeningRepository;

    @InjectMocks
    private ScreeningAnswerCommandService screeningAnswerCommandService;

    private AnswerInsertDto.AnswerInfo buildAnswerInfo(Long screeningId, Long memberId, String content) {
        return AnswerInsertDto.AnswerInfo.builder()
                .id(screeningId)
                .memberId(memberId)
                .content(content)
                .build();
    }

    @Test
    @DisplayName("updateResponseAfterScreening - 기댓값과 제출값 같으면 screened=false")
    void updateResponseAfterScreening_sameAsExpected_notScreened() {
        given(screeningRepository.getScreeningAnswer(10L)).willReturn(true);
        Response response = Response.of(5L, 1L);
        given(responseRepository.findBySurveyIdAndMemberId(5L, 1L)).willReturn(Optional.of(response));

        AnswerInsertDto.AnswerInfo info = buildAnswerInfo(10L, 1L, "true");
        screeningAnswerCommandService.updateResponseAfterScreening(5L, info);

        assertThat(response.getIsScreened()).isFalse();
        verify(responseRepository).save(response);
    }

    @Test
    @DisplayName("updateResponseAfterScreening - 기댓값과 제출값 다르면 screened=true")
    void updateResponseAfterScreening_differsFromExpected_screened() {
        given(screeningRepository.getScreeningAnswer(10L)).willReturn(true);
        Response response = Response.of(5L, 1L);
        given(responseRepository.findBySurveyIdAndMemberId(5L, 1L)).willReturn(Optional.of(response));

        AnswerInsertDto.AnswerInfo info = buildAnswerInfo(10L, 1L, "false");
        screeningAnswerCommandService.updateResponseAfterScreening(5L, info);

        assertThat(response.getIsScreened()).isTrue();
        verify(responseRepository).save(response);
    }

    @Test
    @DisplayName("updateResponseAfterScreening - 기존 응답 없으면 신규 생성 후 저장")
    void updateResponseAfterScreening_noExistingResponse_createsNewAndSaves() {
        given(screeningRepository.getScreeningAnswer(10L)).willReturn(false);
        given(responseRepository.findBySurveyIdAndMemberId(5L, 1L)).willReturn(Optional.empty());

        AnswerInsertDto.AnswerInfo info = buildAnswerInfo(10L, 1L, "false");
        screeningAnswerCommandService.updateResponseAfterScreening(5L, info);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(responseRepository).save(captor.capture());
        assertThat(captor.getValue().getSurveyId()).isEqualTo(5L);
        assertThat(captor.getValue().getMemberId()).isEqualTo(1L);
        assertThat(captor.getValue().getIsScreened()).isFalse();
    }

    @Test
    @DisplayName("insertAnswer - answerRepository.save 호출 후 updateResponseAfterScreening 진행")
    void insertAnswer_savesAnswerAndUpdatesResponse() {
        given(screeningRepository.getSurveyId(10L)).willReturn(5L);
        given(screeningRepository.getScreeningAnswer(10L)).willReturn(true);
        given(answerRepository.save(any())).willReturn(null);
        Response response = Response.of(5L, 1L);
        given(responseRepository.findBySurveyIdAndMemberId(5L, 1L)).willReturn(Optional.of(response));

        AnswerInsertDto.AnswerInfo info = buildAnswerInfo(10L, 1L, "true");
        Boolean result = screeningAnswerCommandService.insertAnswer(info);

        assertThat(result).isTrue();
        verify(answerRepository).save(any());
        verify(responseRepository).save(response);
    }
}
