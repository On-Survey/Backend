package OneQ.OnSurvey.domain.question.service;

import OneQ.OnSurvey.domain.question.entity.ChoiceOption;
import OneQ.OnSurvey.domain.question.entity.Question;
import OneQ.OnSurvey.domain.question.model.dto.OptionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.type.ChoiceDto;
import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;
import OneQ.OnSurvey.domain.question.repository.choiceOption.ChoiceOptionRepository;
import OneQ.OnSurvey.domain.question.repository.question.QuestionRepository;
import OneQ.OnSurvey.domain.survey.model.response.ParticipationQuestionResponse;
import OneQ.OnSurvey.domain.survey.model.response.FormQuestionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionQueryService implements QuestionQuery {
    private final QuestionRepository questionRepository;
    private final ChoiceOptionRepository choiceOptionRepository;

    @Override
    public ParticipationQuestionResponse getQuestionListBySurveyId(Long surveyId) {
        log.info("[QUESTION_SERVICE] 응답하고자 하는 설문 문항 조회 - surveyId: {}", surveyId);

        List<DefaultQuestionDto> questionDtoList = getQuestionDtoList(surveyId);

        return new ParticipationQuestionResponse(questionDtoList);
    }

    @Override
    public List<OptionUpsertDto.OptionInfo> getOptionsByQuestionIdList(List<Long> questionIdList) {
        List<ChoiceOption> optionList = choiceOptionRepository.getOptionsByQuestionIds(questionIdList);

        return optionList.stream().map(OptionUpsertDto::fromEntity).toList();
    }

    @Override
    public FormQuestionResponse getWritingQuestions(Long surveyId) {
        log.info("[QUESTION_SERVICE] 조회할 설문 ID - surveyId: {}", surveyId);

        List<DefaultQuestionDto> questionDtoList = getQuestionDtoList(surveyId);

        return new FormQuestionResponse(surveyId, questionDtoList);
    }

    private List<DefaultQuestionDto> getQuestionDtoList(Long surveyId) {
        List<Question> questionList = questionRepository.getQuestionListBySurveyId(surveyId);
        Set<Long> choiceIdSet = questionList.stream()
            .filter(Question::isChoice)
            .map(Question::getQuestionId)
            .collect(Collectors.toSet());
        log.info("[QUESTION_SERVICE] 조회할 설문 문항 IDs - Ids: {}", choiceIdSet);

        List<ChoiceOption> totalOptionList = choiceOptionRepository.getOptionsByQuestionIds(choiceIdSet);
        Map<Long, List<ChoiceOption>> questionIdChoiceOptionMap = totalOptionList.stream()
            .collect(Collectors.groupingBy(ChoiceOption::getQuestionId));

        List<DefaultQuestionDto> questionDtoList = questionList.stream()
            .map(QuestionConverter::toQuestionDto)
            .toList();

        questionDtoList.forEach(dto -> {
            if (dto.isChoice()) {
                ChoiceDto choiceDto = (ChoiceDto) dto;
                List<ChoiceOption> optionList = questionIdChoiceOptionMap.getOrDefault(dto.getQuestionId(), List.of());
                choiceDto.updateOptions(optionList.stream().map(ChoiceDto::fromEntity).toList());
            }
        });

        return questionDtoList;
    }
}
