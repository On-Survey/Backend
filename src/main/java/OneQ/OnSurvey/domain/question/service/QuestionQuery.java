package OneQ.OnSurvey.domain.question.service;

import OneQ.OnSurvey.domain.question.model.dto.OptionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;

import java.util.List;

public interface QuestionQuery {
    List<OptionUpsertDto.OptionInfo> getOptionsByQuestionIdList(List<Long> questionIdList);
    List<DefaultQuestionDto> getQuestionDtoListBySurveyId(Long surveyId);
}
