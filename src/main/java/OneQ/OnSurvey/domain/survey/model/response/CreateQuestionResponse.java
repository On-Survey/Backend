package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.question.model.QuestionType;
import OneQ.OnSurvey.domain.question.model.dto.QuestionUpsertDto;
import lombok.Builder;

@Builder
public record CreateQuestionResponse (
    Long surveyId,
    Long questionId,
    Integer questionOrder,
    Integer section,
    String title,
    QuestionType type
) {

    public static CreateQuestionResponse fromDto(QuestionUpsertDto dto) {
        QuestionUpsertDto.UpsertInfo info = dto.getUpsertInfoList().getFirst();

        return CreateQuestionResponse.builder()
            .surveyId(dto.getSurveyId())
            .questionId(info.getQuestionId())
            .questionOrder(info.getQuestionOrder())
            .section(info.getSection())
            .title(info.getTitle())
            .type(info.getQuestionType())
            .build();
    }
}
