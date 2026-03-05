package OneQ.OnSurvey.domain.question.service;

import OneQ.OnSurvey.domain.question.entity.ChoiceOption;
import OneQ.OnSurvey.domain.question.entity.Question;
import OneQ.OnSurvey.domain.question.model.dto.OptionDto;
import OneQ.OnSurvey.domain.question.model.dto.type.ChoiceDto;
import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;
import OneQ.OnSurvey.domain.question.repository.choiceOption.ChoiceOptionRepository;
import OneQ.OnSurvey.domain.question.repository.question.QuestionRepository;
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
    public List<OptionDto> getOptionsByQuestionIdList(List<Long> questionIdList) {
        List<ChoiceOption> optionList = choiceOptionRepository.getOptionsByQuestionIds(questionIdList);

        return optionList.stream().map(OptionDto::fromEntity).toList();
    }

    @Override
    public List<DefaultQuestionDto> getQuestionDtoListBySurveyId(Long surveyId) {
        List<Question> questionList = questionRepository.getQuestionListBySurveyId(surveyId);
        log.info("[QUESTION:QUERY:getQuestionDtoListBySurveyId] 조회할 설문 문항 IDs: {}", questionList.stream().map(Question::getQuestionId).toList());

        return fillChoiceOptions(questionList);
    }

    @Override
    public List<DefaultQuestionDto> getQuestionDtoListBySurveyIdAndSection(Long surveyId, Integer section) {
        List<Question> questionList = questionRepository.getQuestionListBySurveyIdAndSection(surveyId, section);
        log.info("[QUESTION:QUERY:getQuestionDtoListBySurveyIdAndSection] 조회할 설문 문항 IDs: {}", questionList.stream().map(Question::getQuestionId).toList());

        return fillChoiceOptions(questionList);
    }

    private List<DefaultQuestionDto> fillChoiceOptions(List<Question> questionList) {

        Set<Long> choiceIdSet = questionList.stream()
            .filter(Question::isChoice)
            .map(Question::getQuestionId)
            .collect(Collectors.toSet());
        log.info("[QUESTION:QUERY:fillChoiceOptions] 선택형 설문 문항 IDs: {}", choiceIdSet);

        List<ChoiceOption> totalOptionList = choiceIdSet.isEmpty() ?
            List.of() : choiceOptionRepository.getOptionsByQuestionIds(choiceIdSet);
        Map<Long, List<ChoiceOption>> questionIdChoiceOptionMap = totalOptionList.stream()
            .collect(Collectors.groupingBy(ChoiceOption::getQuestionId));

        List<DefaultQuestionDto> questionDtoList = questionList.stream()
            .map(QuestionConverter::toQuestionDto)
            .toList();

        questionDtoList.forEach(dto -> {
            if (dto.isChoice()) {
                ChoiceDto choiceDto = (ChoiceDto) dto;
                List<ChoiceOption> optionList = questionIdChoiceOptionMap.getOrDefault(dto.getQuestionId(), List.of());
                choiceDto.updateOptions(optionList.stream().map(OptionDto::fromEntity).toList());
            }
        });

        return questionDtoList;
    }
}
