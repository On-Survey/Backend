package OneQ.OnSurvey.domain.question.service;

import OneQ.OnSurvey.domain.question.model.dto.OptionDto;
import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;

import java.util.List;

public interface QuestionQuery {
    List<OptionDto> getOptionsByQuestionIdList(List<Long> questionIdList);
    List<DefaultQuestionDto> getQuestionDtoListBySurveyId(Long surveyId);
    List<DefaultQuestionDto> getQuestionDtoListBySurveyIdAndSection(Long surveyId, Integer section);
}
