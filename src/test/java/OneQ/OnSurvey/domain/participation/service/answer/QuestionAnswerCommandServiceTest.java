package OneQ.OnSurvey.domain.participation.service.answer;

import OneQ.OnSurvey.domain.participation.entity.QuestionAnswer;
import OneQ.OnSurvey.domain.participation.entity.Response;
import OneQ.OnSurvey.domain.participation.repository.answer.AnswerRepository;
import OneQ.OnSurvey.domain.participation.repository.response.ResponseRepository;
import OneQ.OnSurvey.domain.question.repository.question.QuestionRepository;
import OneQ.OnSurvey.global.infra.redis.RedisAgent;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QuestionAnswerCommandServiceTest {

    @Mock private AnswerRepository<QuestionAnswer> answerRepository;
    @Mock private ResponseRepository responseRepository;
    @Mock private RedisAgent redisAgent;
    @Mock private QuestionRepository questionRepository;

    @InjectMocks
    private QuestionAnswerCommandService questionAnswerCommandService;

    @Test
    @DisplayName("updateResponseAfterQuestionAnswers - 응답 없으면 신규 생성 후 저장")
    void updateResponseAfterQuestionAnswers_notFound_createsAndSaves() {
        given(responseRepository.findBySurveyIdAndMemberId(1L, 2L)).willReturn(Optional.empty());

        questionAnswerCommandService.updateResponseAfterQuestionAnswers(1L, 2L);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(responseRepository).save(captor.capture());
        assertThat(captor.getValue().getSurveyId()).isEqualTo(1L);
        assertThat(captor.getValue().getMemberId()).isEqualTo(2L);
        assertThat(captor.getValue().getIsResponded()).isFalse();
    }

    @Test
    @DisplayName("updateResponseAfterQuestionAnswers - 미완료 응답이면 저장")
    void updateResponseAfterQuestionAnswers_notResponded_saves() {
        Response response = Response.of(1L, 2L);
        given(responseRepository.findBySurveyIdAndMemberId(1L, 2L)).willReturn(Optional.of(response));

        questionAnswerCommandService.updateResponseAfterQuestionAnswers(1L, 2L);

        verify(responseRepository).save(response);
    }

    @Test
    @DisplayName("updateResponseAfterQuestionAnswers - 이미 완료된 응답이면 저장 안함")
    void updateResponseAfterQuestionAnswers_alreadyResponded_doesNotSave() {
        Response response = Response.of(1L, 2L);
        response.markResponded();
        given(responseRepository.findBySurveyIdAndMemberId(1L, 2L)).willReturn(Optional.of(response));

        questionAnswerCommandService.updateResponseAfterQuestionAnswers(1L, 2L);

        verify(responseRepository, never()).save(any(Response.class));
    }
}
