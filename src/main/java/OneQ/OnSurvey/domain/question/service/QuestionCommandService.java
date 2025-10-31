package OneQ.OnSurvey.domain.question.service;

import OneQ.OnSurvey.domain.question.entity.ChoiceOption;
import OneQ.OnSurvey.domain.question.entity.Question;
import OneQ.OnSurvey.domain.question.entity.question.Choice;
import OneQ.OnSurvey.domain.question.entity.question.NPS;
import OneQ.OnSurvey.domain.question.entity.question.Rating;
import OneQ.OnSurvey.domain.question.entity.question.Text;
import OneQ.OnSurvey.domain.question.model.QuestionType;
import OneQ.OnSurvey.domain.question.model.dto.OptionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.QuestionUpsertVO;
import OneQ.OnSurvey.domain.question.repository.choiceOption.ChoiceOptionRepository;
import OneQ.OnSurvey.domain.question.repository.question.QuestionRepository;
import OneQ.OnSurvey.global.exception.CustomException;
import OneQ.OnSurvey.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionCommandService implements QuestionCommand {
    private final QuestionRepository questionRepository;
    private final ChoiceOptionRepository choiceOptionRepository;

    @Override
    public Question createQuestion(Question question) {
        return null;
    }

    @Override
    public Question updateQuestion(Question question) {
        return null;
    }

    @Override
    public Boolean deleteQuestionById(Long questionId) {
        Question question = questionRepository.getQuestionById(questionId);
        question.setDeleted(true);
        questionRepository.save(question);
        return true;
    }

    @Override
    public void changeQuestionOrder(Map<Long, Integer> idOrderMap) {
        // List<Map<Long, Integer>> idOrderMapList = questionList.stream().map(question -> Map.of(question.getQuestionId(), question.getOrder())).toList();
        List<Question> questionList = questionRepository.getQuestionsByIds(idOrderMap.keySet());

        questionList.forEach(question -> {
            Integer order = idOrderMap.get(question.getQuestionId());
            question.updateOrder(order);
        });

        questionRepository.saveAll(questionList);
    }

    @Override
    public List<Question> upsertQuestionList(QuestionUpsertVO upsertVO) {
        Long surveyId = upsertVO.getSurveyId();

        Map<Boolean, List<QuestionUpsertVO.UpsertInfo>> partitionUpsertInfoList
            = upsertVO.getUpsertInfoList().stream().collect(Collectors.partitioningBy(info -> info.getQuestionId() != null));
        Map<Long, QuestionUpsertVO.UpsertInfo> idInfoMap = partitionUpsertInfoList.get(true).stream().collect(Collectors.toMap(
            QuestionUpsertVO.UpsertInfo::getQuestionId,
            Function.identity(),
            (existing, replace) -> existing
        ));
        List<Question> saveList = questionRepository.getQuestionsByIds(idInfoMap.keySet());

        saveList.forEach(question -> {
            Long id = question.getQuestionId();
            QuestionUpsertVO.UpsertInfo upsertInfo = idInfoMap.get(id);
            updateQuestion(upsertInfo, question);
        });

        List<Question> createdList = partitionUpsertInfoList.get(false).stream().map(upsertInfo -> createQuestion(surveyId, upsertInfo)).toList();
        saveList.addAll(createdList);

        return questionRepository.saveAll(saveList);
    }

    private void updateQuestion(QuestionUpsertVO.UpsertInfo upsertInfo, Question question) {
        if (question instanceof Choice choice) {
            choice.updateQuestion(
                upsertInfo.getTitle(),
                upsertInfo.getDescription(),
                upsertInfo.getIsRequired(),
                upsertInfo.getOrder(),
                upsertInfo.getMaxChoice(),
                upsertInfo.getHasNoneOption(),
                upsertInfo.getHasCustomInput()
            );
        } else if (question instanceof Rating rating) {
            rating.updateQuestion(
                upsertInfo.getTitle(),
                upsertInfo.getDescription(),
                upsertInfo.getIsRequired(),
                upsertInfo.getOrder(),
                upsertInfo.getMaxValue(),
                upsertInfo.getMinValue()
            );
        } else if (question instanceof NPS nps) {
            nps.updateQuestion(
                upsertInfo.getTitle(),
                upsertInfo.getDescription(),
                upsertInfo.getIsRequired(),
                upsertInfo.getOrder()
            );
        } else if (question instanceof Text text) {
            text.updateQuestion(
                upsertInfo.getTitle(),
                upsertInfo.getDescription(),
                upsertInfo.getIsRequired(),
                upsertInfo.getOrder(),
                upsertInfo.getDefaultValue()
            );
        }
    }

    private Question createQuestion(Long surveyId, QuestionUpsertVO.UpsertInfo upsertInfo) {
        QuestionType type = upsertInfo.getQuestionType();

        if (type.equals(QuestionType.TEXT)) {
            return Text.of(
                surveyId,
                upsertInfo.getOrder(),
                upsertInfo.getTitle(),
                upsertInfo.getDescription(),
                upsertInfo.getIsRequired(),
                upsertInfo.getTextType()
            );
        } else if (type.equals(QuestionType.NPS)) {
            return NPS.of(
                surveyId,
                upsertInfo.getOrder(),
                upsertInfo.getTitle(),
                upsertInfo.getDescription(),
                upsertInfo.getIsRequired()
            );
        } else if (type.equals(QuestionType.RATING)) {
            return Rating.of(
                surveyId,
                upsertInfo.getOrder(),
                upsertInfo.getTitle(),
                upsertInfo.getDescription(),
                upsertInfo.getIsRequired(),
                upsertInfo.getMaxValue(),
                upsertInfo.getMinValue()
            );
        } else if (type.equals(QuestionType.CHOICE)) {
            return Choice.of(
                surveyId,
                upsertInfo.getOrder(),
                upsertInfo.getTitle(),
                upsertInfo.getDescription(),
                upsertInfo.getIsRequired(),
                upsertInfo.getMaxChoice(),
                upsertInfo.getHasNoneOption(),
                upsertInfo.getHasCustomInput()
            );
        } else {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    @Override
    public List<ChoiceOption> upsertChoiceOptionList(OptionUpsertDto upsertVO) {
        Long questionId = upsertVO.getQuestionId();

        Map<Boolean, List<OptionUpsertDto.UpsertInfo>> partitionUpsertInfoList
            = upsertVO.getUpsertInfoList().stream().collect(Collectors.partitioningBy(info -> info.getOptionId() != null));

        Map<Long, OptionUpsertDto.UpsertInfo> idInfoMap = partitionUpsertInfoList.get(true).stream().collect(Collectors.toMap(
            OptionUpsertDto.UpsertInfo::getOptionId,
            Function.identity(),
            (existing, replace) -> existing
        ));
        List<ChoiceOption> saveList = choiceOptionRepository.getOptionsByIds(idInfoMap.keySet());

        saveList.forEach(option -> {
            Long id = option.getChoiceOptionId();
            OptionUpsertDto.UpsertInfo upsertInfo = idInfoMap.get(id);
            option.updateOption(
                upsertInfo.getContent(),
                upsertInfo.getNextQuestionId()
            );
        });

        List<ChoiceOption> createdList = partitionUpsertInfoList.get(false).stream()
            .map(upsertInfo -> ChoiceOption.of(
                questionId,
                upsertInfo.getContent(),
                upsertInfo.getNextQuestionId()
            )).toList();
        saveList.addAll(createdList);

        return choiceOptionRepository.saveAll(saveList);
    }
}
