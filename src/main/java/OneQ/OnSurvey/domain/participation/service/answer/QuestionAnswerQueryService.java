package OneQ.OnSurvey.domain.participation.service.answer;

import OneQ.OnSurvey.domain.participation.entity.QuestionAnswer;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerStats;
import OneQ.OnSurvey.domain.participation.repository.answer.AnswerRepository;
import OneQ.OnSurvey.domain.question.model.QuestionType;
import OneQ.OnSurvey.domain.survey.model.SurveyResponseFilterCondition;
import OneQ.OnSurvey.domain.survey.model.response.SurveyManagementDetailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class QuestionAnswerQueryService extends AnswerQueryService<QuestionAnswer> {
    public QuestionAnswerQueryService(
        AnswerRepository<QuestionAnswer> answerRepository
    ) {
        super(answerRepository);
    }

    @Override
    public List<SurveyManagementDetailResponse.DetailInfo> getDetailInfo(
            Long surveyId,
            List<SurveyManagementDetailResponse.DetailInfo> detailInfoList
    ) {
        return getDetailInfo(surveyId, null, detailInfoList);
    }

    @Override
    public List<SurveyManagementDetailResponse.DetailInfo> getDetailInfo(
            Long surveyId,
            SurveyResponseFilterCondition filter,
            List<SurveyManagementDetailResponse.DetailInfo> detailInfoList
    ) {
        log.info("[QUESTION_ANSWER_SERVICE] 문항 별 응답결과 조회 - surveyId: {}, filter: {}", surveyId, filter);

        Map<Boolean, List<SurveyManagementDetailResponse.DetailInfo>> typeInfoMap = detailInfoList.stream()
                .collect(Collectors.partitioningBy(detailInfo -> detailInfo.getType().isText()));

        List<Long> nonTextQuestionIdList = typeInfoMap.get(false).stream()
                .map(SurveyManagementDetailResponse.DetailInfo::getQuestionId)
                .toList();

        List<Long> textQuestionIdList = typeInfoMap.get(true).stream()
                .map(SurveyManagementDetailResponse.DetailInfo::getQuestionId)
                .toList();

        log.info("[QUESTION_ANSWER_SERVICE] 응답을 조회할 문항 IDs - 주관식: {}, 비주관식: {}",
                textQuestionIdList, nonTextQuestionIdList);

        List<AnswerStats> nonTextAnswerStats = nonTextQuestionIdList.isEmpty()
                ? List.of()
                : answerRepository.getAggregatedAnswersByQuestionIds(nonTextQuestionIdList, filter);

        List<AnswerStats> textAnswerStats = textQuestionIdList.isEmpty()
                ? List.of()
                : answerRepository.getAnswersByQuestionIds(textQuestionIdList, filter);

        Map<Long, Map<String, Long>> nonTextAnswerMap = nonTextAnswerStats.stream()
                .collect(Collectors.groupingBy(
                        AnswerStats::getQuestionId,
                        Collectors.toMap(
                                AnswerStats::getContent,
                                AnswerStats::getCount
                        )
                ));

        Map<Long, List<String>> textAnswerMap = textAnswerStats.stream()
                .collect(Collectors.groupingBy(
                        AnswerStats::getQuestionId,
                        Collectors.mapping(AnswerStats::getContent, Collectors.toList())
                ));

        detailInfoList.forEach(detailInfo -> {
            Long questionId = detailInfo.getQuestionId();
            QuestionType questionType = detailInfo.getType();

            if (questionType.isText()) {
                detailInfo.setAnswerList(
                        textAnswerMap.getOrDefault(questionId, List.of())
                );

            } else if (questionType.isChoice()) {
                Map<String, Long> frame = detailInfo.getAnswerMap();
                Map<String, Long> answerMap = nonTextAnswerMap.getOrDefault(questionId, Map.of());

                frame.keySet().forEach(key ->
                        frame.put(key, answerMap.getOrDefault(key, 0L))
                );

                List<String> etcList = new ArrayList<>();
                answerMap.entrySet().stream()
                        .filter(entry -> !frame.containsKey(entry.getKey()))
                        .forEach(entry -> IntStream.range(0, entry.getValue().intValue())
                                .forEach((ignored) -> etcList.add(entry.getKey())));

                detailInfo.setAnswerList(etcList);

            } else { // 평가형, NPS
                Map<String, Long> answerMap = nonTextAnswerMap.getOrDefault(questionId, Map.of());
                detailInfo.setAnswerMap(answerMap);
            }
        });

        return detailInfoList;
    }
}
