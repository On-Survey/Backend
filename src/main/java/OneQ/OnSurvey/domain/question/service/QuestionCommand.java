package OneQ.OnSurvey.domain.question.service;

import OneQ.OnSurvey.domain.question.model.dto.OptionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.QuestionUpsertDto;

import java.util.List;

public interface QuestionCommand {
    QuestionUpsertDto upsertQuestionList(QuestionUpsertDto upsertDto);
    List<OptionUpsertDto> upsertChoiceOptionList(List<OptionUpsertDto> upsertDtoList);
}
