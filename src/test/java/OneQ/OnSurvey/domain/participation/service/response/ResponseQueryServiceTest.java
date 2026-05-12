package OneQ.OnSurvey.domain.participation.service.response;

import OneQ.OnSurvey.domain.participation.repository.response.ResponseRepository;
import OneQ.OnSurvey.domain.survey.model.AgeRange;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.Residence;
import OneQ.OnSurvey.domain.survey.model.SurveyResponseFilterCondition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ResponseQueryServiceTest {

    @Mock
    private ResponseRepository responseRepository;

    @InjectMocks
    private ResponseQueryService responseQueryService;

    @Test
    @DisplayName("getResponseCountBySurveyId (필터 없음) - repository에 위임")
    void getResponseCount_noFilter_delegatesToRepository() {
        given(responseRepository.getResponseCountBySurveyId(1L)).willReturn(50);

        Integer result = responseQueryService.getResponseCountBySurveyId(1L);

        assertThat(result).isEqualTo(50);
        verify(responseRepository).getResponseCountBySurveyId(1L);
    }

    @Test
    @DisplayName("getResponseCountBySurveyId - filter가 null이면 필터 없는 메서드 위임")
    void getResponseCount_nullFilter_delegatesToNoFilter() {
        given(responseRepository.getResponseCountBySurveyId(1L)).willReturn(30);

        Integer result = responseQueryService.getResponseCountBySurveyId(1L, null);

        assertThat(result).isEqualTo(30);
        verify(responseRepository).getResponseCountBySurveyId(1L);
        verify(responseRepository, never()).getResponseCountBySurveyId(anyLong(), any());
    }

    @Test
    @DisplayName("getResponseCountBySurveyId - filter가 비어있으면 필터 없는 메서드 위임")
    void getResponseCount_emptyFilter_delegatesToNoFilter() {
        SurveyResponseFilterCondition empty = SurveyResponseFilterCondition.empty();
        given(responseRepository.getResponseCountBySurveyId(1L)).willReturn(20);

        Integer result = responseQueryService.getResponseCountBySurveyId(1L, empty);

        assertThat(result).isEqualTo(20);
        verify(responseRepository).getResponseCountBySurveyId(1L);
    }

    @Test
    @DisplayName("getResponseCountBySurveyId - 유효한 filter면 필터 포함 메서드 위임")
    void getResponseCount_withFilter_delegatesToFilteredMethod() {
        SurveyResponseFilterCondition filter = new SurveyResponseFilterCondition(
                List.of(AgeRange.TWENTY), List.of(Gender.MALE), List.of(Residence.SEOUL)
        );
        given(responseRepository.getResponseCountBySurveyId(1L, filter)).willReturn(10);

        Integer result = responseQueryService.getResponseCountBySurveyId(1L, filter);

        assertThat(result).isEqualTo(10);
        verify(responseRepository).getResponseCountBySurveyId(1L, filter);
    }
}
