package OneQ.OnSurvey.domain.question.service;

import OneQ.OnSurvey.domain.question.entity.Question;
import OneQ.OnSurvey.domain.question.repository.question.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionQueryService implements QuestionQuery {
    private final QuestionRepository questionRepository;

    @Override
    public List<Question> getQuestionListBySurveyId(Long surveyId) {
        return questionRepository.getQuestionListBySurveyId(surveyId);
    }
}
