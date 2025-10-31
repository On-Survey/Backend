package OneQ.OnSurvey.domain.question.service;

import OneQ.OnSurvey.domain.question.entity.ChoiceOption;
import OneQ.OnSurvey.domain.question.entity.Question;
import OneQ.OnSurvey.domain.question.model.dto.OptionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.QuestionUpsertVO;

import java.util.List;
import java.util.Map;

public interface QuestionCommand {
    Question createQuestion(Question question); // 문항 ID 생성
    Question updateQuestion(Question question); // 문항 ID 기반 기본 정보 입력

    Boolean deleteQuestionById(Long questionId);

    void changeQuestionOrder(Map<Long, Integer> idOrderMap);
    List<Question> upsertQuestionList(QuestionUpsertVO upsertVO);

    List<ChoiceOption> upsertChoiceOptionList(OptionUpsertDto upsertVO);
}
