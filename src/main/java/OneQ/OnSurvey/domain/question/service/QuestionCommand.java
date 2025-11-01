package OneQ.OnSurvey.domain.question.service;

import OneQ.OnSurvey.domain.question.entity.ChoiceOption;
import OneQ.OnSurvey.domain.question.entity.Question;
import OneQ.OnSurvey.domain.question.model.dto.OptionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.QuestionUpsertDto;

import java.util.List;
import java.util.Map;

public interface QuestionCommand {
    Boolean deleteQuestionById(Long questionId);

    void changeQuestionOrder(Map<Long, Integer> idOrderMap);
    List<Question> upsertQuestionList(QuestionUpsertDto upsertVO);

    List<ChoiceOption> upsertChoiceOptionList(OptionUpsertDto upsertVO);
}
