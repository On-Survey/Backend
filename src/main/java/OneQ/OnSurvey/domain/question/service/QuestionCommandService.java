package OneQ.OnSurvey.domain.question.service;

import OneQ.OnSurvey.domain.question.entity.ChoiceOption;
import OneQ.OnSurvey.domain.question.entity.Question;
import OneQ.OnSurvey.domain.question.entity.question.Choice;
import OneQ.OnSurvey.domain.question.entity.question.NPS;
import OneQ.OnSurvey.domain.question.entity.question.NumberAnswer;
import OneQ.OnSurvey.domain.question.entity.question.Rating;
import OneQ.OnSurvey.domain.question.entity.question.ShortAnswer;
import OneQ.OnSurvey.domain.question.entity.question.DateAnswer;
import OneQ.OnSurvey.domain.question.entity.question.LongAnswer;
import OneQ.OnSurvey.domain.question.model.QuestionType;
import OneQ.OnSurvey.domain.question.model.dto.OptionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.QuestionUpsertDto;
import OneQ.OnSurvey.domain.question.repository.choiceOption.ChoiceOptionRepository;
import OneQ.OnSurvey.domain.question.repository.question.QuestionRepository;
import OneQ.OnSurvey.global.exception.CustomException;
import OneQ.OnSurvey.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QuestionCommandService implements QuestionCommand {
    private final QuestionRepository questionRepository;
    private final ChoiceOptionRepository choiceOptionRepository;

    @Override
    public Boolean deleteQuestionById(Long questionId) {
        Question question = questionRepository.getQuestionById(questionId);
        question.setDeleted(true);
        questionRepository.save(question);
        return true;
    }

    @Override
    public void changeQuestionOrder(Map<Long, Integer> idOrderMap) {
        // List<Map<Long, Integer>> idOrderMapList = questionList.stream().map(question -> Map.of(question.getQuestionId(), question.getQuestionOrder())).toList();
        List<Question> questionList = questionRepository.getQuestionsByIds(idOrderMap.keySet());

        questionList.forEach(question -> {
            Integer order = idOrderMap.get(question.getQuestionId());
            question.updateOrder(order);
        });

        questionRepository.saveAll(questionList);
    }

    @Override
    public QuestionUpsertDto upsertQuestionList(QuestionUpsertDto upsertDto) {
        log.info("[QUESTION:COMMAND:upsertQuestionList] 문항 UPSERT - surveyId: {}, upsertInfoList: {}", upsertDto.getSurveyId(), upsertDto.getUpsertInfoList().toString());

        Long surveyId = upsertDto.getSurveyId();
        List<QuestionUpsertDto.UpsertInfo> upsertInfoList = upsertDto.getUpsertInfoList();

        // 1. DB 저장 문항 전체 조회
        List<Question> prevQuestionList = questionRepository.getQuestionListBySurveyId(surveyId);

        // 2. Insert/Update 데이터 파티셔닝
        Map<Boolean, List<QuestionUpsertDto.UpsertInfo>> partitionUpsertInfoList = upsertInfoList.stream()
            .collect(Collectors.partitioningBy(info -> info.getQuestionId() != null));
        List<QuestionUpsertDto.UpsertInfo> newInfoList = partitionUpsertInfoList.get(false);
        List<QuestionUpsertDto.UpsertInfo> updateInfoList = partitionUpsertInfoList.get(true);

        // 3. Update 대상 ID 추출
        Set<Long> updateIdSet = updateInfoList.stream()
            .map(QuestionUpsertDto.UpsertInfo::getQuestionId)
            .collect(Collectors.toSet());

        log.info("[QUESTION:COMMAND:upsertQuestionList] 수정되는 문항 IDs: {}", updateIdSet);
        log.info("[QUESTION:COMMAND:upsertQuestionList] 생성되는 문항 개수: {}", newInfoList.size());

        // 4. Delete 대상 ID 추출 및 삭제
        if (newInfoList.isEmpty()) {
            Set<Long> deleteIdSet = prevQuestionList.stream()
                .map(Question::getQuestionId)
                .filter(questionId -> !updateIdSet.contains(questionId))
                .collect(Collectors.toSet());

            log.info("[QUESTION:COMMAND:upsertQuestionList] 삭제되는 문항 IDs: {}", deleteIdSet);
            questionRepository.deleteAll(deleteIdSet);

            log.info("[QUESTION:COMMAND:upsertQuestionList] DELETE 진행");
        }

        // 5. Update 대상 수정
        Map<Long, QuestionUpsertDto.UpsertInfo> updateInfoMap = updateInfoList.stream().collect(Collectors.toMap(
            QuestionUpsertDto.UpsertInfo::getQuestionId,
            Function.identity(),
            (existing, replace) -> existing
        ));
        List<Question> updateList = prevQuestionList.stream()
            .filter(question -> updateIdSet.contains(question.getQuestionId()))
            .toList();

        updateList.forEach(question -> {
            Long id = question.getQuestionId();
            QuestionUpsertDto.UpsertInfo upsertInfo = updateInfoMap.get(id);
            updateQuestion(upsertInfo, question);
        });

        // 6. Insert 대상 객체 생성
        List<Question> insertList = newInfoList.stream()
            .map(upsertInfo -> createQuestion(surveyId, upsertInfo))
            .toList();

        List<Question> finalList = new ArrayList<>(updateList);
        finalList.addAll(insertList);

        // 7. Update/Insert 진행
        updateList = questionRepository.saveAll(finalList);

        log.info("[QUESTION:COMMAND:upsertQuestionList] UPSERT 완료");

        // 8. 반환값 구성
        upsertInfoList = updateList.stream()
            .sorted(Comparator.comparingInt(Question::getOrder))
            .map(QuestionUpsertDto::fromEntity)
            .toList();

        return QuestionUpsertDto.builder()
            .surveyId(surveyId)
            .upsertInfoList(upsertInfoList)
            .build();
    }

    private void updateQuestion(QuestionUpsertDto.UpsertInfo upsertInfo, Question question) {
        if (question instanceof Choice choice) {
            choice.updateQuestion(
                upsertInfo.getTitle(),
                upsertInfo.getDescription(),
                upsertInfo.getIsRequired(),
                upsertInfo.getQuestionOrder(),
                upsertInfo.getMaxChoice(),
                upsertInfo.getHasNoneOption(),
                upsertInfo.getHasCustomInput()
            );
        } else if (question instanceof Rating rating) {
            rating.updateQuestion(
                upsertInfo.getTitle(),
                upsertInfo.getDescription(),
                upsertInfo.getIsRequired(),
                upsertInfo.getQuestionOrder(),
                upsertInfo.getMaxValue(),
                upsertInfo.getMinValue()
            );
        } else if (question instanceof NPS nps) {
            nps.updateQuestion(
                upsertInfo.getTitle(),
                upsertInfo.getDescription(),
                upsertInfo.getIsRequired(),
                upsertInfo.getQuestionOrder()
            );
        } else if (question instanceof DateAnswer date) {
            date.updateQuestion(
                upsertInfo.getTitle(),
                upsertInfo.getDescription(),
                upsertInfo.getIsRequired(),
                upsertInfo.getQuestionOrder(),
                upsertInfo.getDefaultDate()
            );
        } else if (question instanceof ShortAnswer shortAnswer) {
            shortAnswer.updateQuestion(
                upsertInfo.getTitle(),
                upsertInfo.getDescription(),
                upsertInfo.getIsRequired(),
                upsertInfo.getQuestionOrder()
            );
        } else if (question instanceof LongAnswer longAnswer) {
            longAnswer.updateQuestion(
                upsertInfo.getTitle(),
                upsertInfo.getDescription(),
                upsertInfo.getIsRequired(),
                upsertInfo.getQuestionOrder()
            );
        } else if (question instanceof NumberAnswer numberAnswer) {
            numberAnswer.updateQuestion(
                upsertInfo.getTitle(),
                upsertInfo.getDescription(),
                upsertInfo.getIsRequired(),
                upsertInfo.getQuestionOrder()
            );
        }
    }

    private Question createQuestion(Long surveyId, QuestionUpsertDto.UpsertInfo upsertInfo) {
        QuestionType type = upsertInfo.getQuestionType();

        if (QuestionType.DATE.equals(type)) {
            return DateAnswer.of(
                surveyId,
                upsertInfo.getQuestionOrder(),
                upsertInfo.getTitle(),
                upsertInfo.getDescription(),
                upsertInfo.getIsRequired(),
                upsertInfo.getDefaultDate(),
                type
            );
        } else if (QuestionType.NPS.equals(type)) {
            return NPS.of(
                surveyId,
                upsertInfo.getQuestionOrder(),
                upsertInfo.getTitle(),
                upsertInfo.getDescription(),
                upsertInfo.getIsRequired(),
                type
            );
        } else if (QuestionType.RATING.equals(type)) {
            return Rating.of(
                surveyId,
                upsertInfo.getQuestionOrder(),
                upsertInfo.getTitle(),
                upsertInfo.getDescription(),
                upsertInfo.getIsRequired(),
                upsertInfo.getMaxValue(),
                upsertInfo.getMinValue(),
                type
            );
        } else if (QuestionType.CHOICE.equals(type)) {
            return Choice.of(
                surveyId,
                upsertInfo.getQuestionOrder(),
                upsertInfo.getTitle(),
                upsertInfo.getDescription(),
                upsertInfo.getIsRequired(),
                upsertInfo.getMaxChoice(),
                upsertInfo.getHasNoneOption(),
                upsertInfo.getHasCustomInput(),
                type
            );
        } else if (QuestionType.SHORT.equals(type)) {
            return ShortAnswer.of(
                surveyId,
                upsertInfo.getQuestionOrder(),
                upsertInfo.getTitle(),
                upsertInfo.getDescription(),
                upsertInfo.getIsRequired(),
                type
            );
        } else if (QuestionType.LONG.equals(type)) {
            return LongAnswer.of(
                surveyId,
                upsertInfo.getQuestionOrder(),
                upsertInfo.getTitle(),
                upsertInfo.getDescription(),
                upsertInfo.getIsRequired(),
                type
            );
        } else if (QuestionType.NUMBER.equals(type)) {
            return NumberAnswer.of(
                surveyId,
                upsertInfo.getQuestionOrder(),
                upsertInfo.getTitle(),
                upsertInfo.getDescription(),
                upsertInfo.getIsRequired(),
                type
            );
        } else {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    @Override
    public List<OptionUpsertDto> upsertChoiceOptionList(List<OptionUpsertDto> upsertDtoList) {
        log.info("[QUESTION:COMMAND:upsertChoiceOptionList] 보기 UPSERT - upsertDtoList : {}", upsertDtoList.toString());

        List<ChoiceOption> finalList = new ArrayList<>();

        for (OptionUpsertDto upsertDto : upsertDtoList) {
            Long questionId = upsertDto.getQuestionId();
            List<OptionUpsertDto.OptionInfo> requestInfos = upsertDto.getOptionInfoList();

            // 1. DB 저장 보기 전체 조회
            List<ChoiceOption> prevOptionList = choiceOptionRepository.getOptionsByQuestionId(questionId);

            // 2. Insert/Update 데이터 파티셔닝
            Map<Boolean, List<OptionUpsertDto.OptionInfo>> partitionUpsertInfoList = requestInfos.stream()
                .collect(Collectors.partitioningBy(info -> info.getOptionId() != null));

            List<OptionUpsertDto.OptionInfo> newInfoList = partitionUpsertInfoList.get(false);
            List<OptionUpsertDto.OptionInfo> updateInfoList = partitionUpsertInfoList.get(true);

            // 3. Update 대상 ID 추출
            Set<Long> updateIdSet = updateInfoList.stream()
                .map(OptionUpsertDto.OptionInfo::getOptionId)
                .collect(Collectors.toSet());

            log.info("[QUESTION:COMMAND:upsertChoiceOptionList] 수정되는 문항: {}, 보기 IDs: {}", questionId, updateIdSet);
            log.info("[QUESTION:COMMAND:upsertChoiceOptionList] 생성되는 문항: {}, 보기 개수: {}", questionId, newInfoList.size());

            // 4. Delete 대상 ID 추출 및 삭제
            Set<Long> deleteIdSet = prevOptionList.stream()
                .map(ChoiceOption::getChoiceOptionId)
                .filter(optionId -> !updateIdSet.contains(optionId))
                .collect(Collectors.toSet());
            log.info("[QUESTION:COMMAND:upsertChoiceOptionList] 삭제되는 문항: {}, 보기 IDs: {}", questionId, deleteIdSet);

            choiceOptionRepository.deleteAll(deleteIdSet);

            log.info("[QUESTION:COMMAND:upsertChoiceOptionList] DELETE 진행");

            // 5. Update 대상 수정
            Map<Long, OptionUpsertDto.OptionInfo> updateInfoMap = updateInfoList.stream().collect(Collectors.toMap(
                OptionUpsertDto.OptionInfo::getOptionId,
                Function.identity(),
                (existing, replace) -> existing
            ));
            List<ChoiceOption> updateList = prevOptionList.stream()
                .filter(option -> updateIdSet.contains(option.getChoiceOptionId()))
                .toList();

            updateList.forEach(option -> {
                Long id = option.getChoiceOptionId();
                OptionUpsertDto.OptionInfo optionInfo = updateInfoMap.get(id);
                option.updateOption(
                    optionInfo.getContent(),
                    optionInfo.getNextQuestionId()
                );
            });

            // 6. Insert 대상 객체 생성
            List<ChoiceOption> insertList = newInfoList.stream()
                .map(upsertInfo -> ChoiceOption.of(
                    questionId,
                    upsertInfo.getContent(),
                    upsertInfo.getNextQuestionId()
                )).toList();

            finalList.addAll(updateList);
            finalList.addAll(insertList);
        }

        // 7. Update/Insert 진행
        List<ChoiceOption> optionList = choiceOptionRepository.saveAll(finalList);

        log.info("[QUESTION:COMMAND:upsertChoiceOptionList] UPSERT 완료");

        // 8. 반환값 구성
        Map<Long, List<ChoiceOption>> idOptionListMap = optionList.stream().collect(Collectors.groupingBy(ChoiceOption::getQuestionId));
        return idOptionListMap.entrySet().stream().map(entry -> {
            Long questionId = entry.getKey();
            List<ChoiceOption> savedList = entry.getValue();

            return OptionUpsertDto.builder()
                .questionId(questionId)
                .optionInfoList(savedList.stream().map(OptionUpsertDto::fromEntity).toList())
                .build();
            })
            .toList();
    }
}
