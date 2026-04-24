package OneQ.OnSurvey.domain.question.service;

import OneQ.OnSurvey.domain.question.entity.ChoiceOption;
import OneQ.OnSurvey.domain.question.entity.GridOption;
import OneQ.OnSurvey.domain.question.entity.Question;
import OneQ.OnSurvey.domain.question.model.QuestionType;
import OneQ.OnSurvey.domain.question.model.dto.GridOptionDto;
import OneQ.OnSurvey.domain.question.model.dto.OptionDto;
import OneQ.OnSurvey.domain.question.model.dto.type.ChoiceDto;
import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;
import OneQ.OnSurvey.domain.question.model.dto.type.GridDto;
import OneQ.OnSurvey.domain.question.repository.choiceOption.ChoiceOptionRepository;
import OneQ.OnSurvey.domain.question.repository.gridOption.GridOptionRepository;
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
    private final GridOptionRepository gridOptionRepository;

    @Override
    public List<OptionDto> getOptionsByQuestionIdList(List<Long> questionIdList) {
        List<ChoiceOption> optionList = choiceOptionRepository.getOptionsByQuestionIds(questionIdList);

        return optionList.stream().map(OptionDto::fromEntity).toList();
    }

    @Override
    public List<GridOptionDto> getGridOptionsByQuestionIdList(List<Long> questionIdList) {
        List<GridOption> gridOptionList = gridOptionRepository.getGridOptionsByQuestionIds(questionIdList);

        return gridOptionList.stream().map(GridOptionDto::fromEntity).toList();
    }

    @Override
    public List<DefaultQuestionDto> getQuestionDtoListBySurveyId(Long surveyId) {
        List<Question> questionList = questionRepository.getQuestionListBySurveyId(surveyId);
        log.info("[QUESTION:QUERY:getQuestionDtoListBySurveyId] 조회할 설문 문항 IDs: {}", questionList.stream().map(Question::getQuestionId).toList());

        return fillOptions(questionList);
    }

    @Override
    public List<DefaultQuestionDto> getQuestionDtoListBySurveyIdAndSection(Long surveyId, Integer section) {
        List<Question> questionList = questionRepository.getQuestionListBySurveyIdAndSection(surveyId, section);
        log.info("[QUESTION:QUERY:getQuestionDtoListBySurveyIdAndSection] 조회할 설문 문항 IDs: {}", questionList.stream().map(Question::getQuestionId).toList());

        return fillOptions(questionList);
    }

    @Override
    public int countQuestionsBySurveyId(Long surveyId) {
        return questionRepository.countBySurveyId(surveyId);
    }

    private List<DefaultQuestionDto> fillOptions(List<Question> questionList) {
        Map<QuestionType, Set<Long>> typeIdMap = questionList.stream()
            .filter(q -> QuestionType.CHOICE.equals(q.getQuestionType()) || QuestionType.GRID.equals(q.getQuestionType()))
            .collect(Collectors.groupingBy(
                Question::getQuestionType,
                Collectors.mapping(Question::getQuestionId, Collectors.toSet())
            ));

        Map<Long, List<ChoiceOption>> choiceIdOptionMap = typeIdMap.getOrDefault(QuestionType.CHOICE, Set.of()).isEmpty()
            ? Map.of()
            : choiceOptionRepository.getOptionsByQuestionIds(typeIdMap.get(QuestionType.CHOICE))
                .stream()
                .collect(Collectors.groupingBy(ChoiceOption::getQuestionId));
        Map<Long, List<GridOption>> gridIdOptionMap = typeIdMap.getOrDefault(QuestionType.GRID, Set.of()).isEmpty()
            ? Map.of()
            : gridOptionRepository.getGridOptionsByQuestionIds(typeIdMap.get(QuestionType.GRID))
                .stream()
                .collect(Collectors.groupingBy(GridOption::getQuestionId));

        return questionList.stream()
            .map(QuestionConverter::toQuestionDto)
            .peek(dto -> {
                if (dto.isChoice()) {
                    ChoiceDto choiceDto = (ChoiceDto) dto;
                    List<ChoiceOption> optionList = choiceIdOptionMap.getOrDefault(dto.getQuestionId(), List.of());
                    choiceDto.updateOptions(optionList.stream().map(OptionDto::fromEntity).toList());
                } else if (dto.isGrid()) {
                    GridDto gridDto = (GridDto) dto;
                    List<GridOption> gridOptionList = gridIdOptionMap.getOrDefault(dto.getQuestionId(), List.of());
                    gridDto.updateOptions(gridOptionList.stream().map(GridOptionDto::fromEntity).toList());
                }
            })
            .toList();
    }
}
