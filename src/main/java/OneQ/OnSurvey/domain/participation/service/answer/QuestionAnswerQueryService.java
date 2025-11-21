package OneQ.OnSurvey.domain.participation.service.answer;

import OneQ.OnSurvey.domain.participation.entity.QuestionAnswer;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerStats;
import OneQ.OnSurvey.domain.participation.repository.answer.AnswerRepository;
import OneQ.OnSurvey.domain.survey.model.response.SurveyManagementDetailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class QuestionAnswerQueryService extends AnswerQueryService<QuestionAnswer> {
    public QuestionAnswerQueryService(
        AnswerRepository<QuestionAnswer> answerRepository
    ) {
        super(answerRepository);
    }

    @Override
    public QuestionAnswer getAnswerById(Long questionId, Long memberId) {
        return null;
    }

    @Override
    public QuestionAnswer createAnswerFromDto(AnswerInsertDto.AnswerInfo answerInfo) {
        return QuestionAnswer.from(answerInfo);
    }

    @Override
    public List<SurveyManagementDetailResponse.DetailInfo> getDetailInfo(
        Long surveyId,
        List<SurveyManagementDetailResponse.DetailInfo> detailInfoList
    ) {
        log.info("[QUESTION_ANSWER_SERVICE] 문항 별 응답결과 조회 - surveyId: {}", surveyId);

        Map<Boolean, List<SurveyManagementDetailResponse.DetailInfo>> typeInfoMap = detailInfoList.stream()
            .collect(Collectors.partitioningBy(detailInfo -> detailInfo.getType().isText()));

        List<Long> nonTextQuestionIdList = typeInfoMap.get(false).stream()
            .map(SurveyManagementDetailResponse.DetailInfo::getQuestionId)
            .toList();

        List<Long> textQuestionIdList = typeInfoMap.get(true).stream()
            .map(SurveyManagementDetailResponse.DetailInfo::getQuestionId)
            .toList();

        log.info("[QUESTION_ANSWER_SERVICE] 응답을 조회할 문항 IDs - 주관식: {}, 비주관식: {}", textQuestionIdList, nonTextQuestionIdList);

        List<AnswerStats> nonTextAnswerStats = nonTextQuestionIdList.isEmpty() ?
            List.of() : answerRepository.getAggregatedAnswersByQuestionIds(nonTextQuestionIdList);
        List<AnswerStats> textAnswerStats = textQuestionIdList.isEmpty() ?
            List.of() : answerRepository.getAnswersByQuestionIds(textQuestionIdList);

        // 임시 로그
        log.info("[QUESTION_ANSWER_SERVICE] nonTextAnswerStats questionIds: {}",
                nonTextAnswerStats.stream()
                        .map(AnswerStats::getQuestionId)
                        .distinct()
                        .toList()
        );
        //

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
            if (detailInfo.getType().isText()) {
                detailInfo.setAnswerList(textAnswerMap.getOrDefault(questionId, List.of()));
            } else {
                detailInfo.setAnswerMap(nonTextAnswerMap.getOrDefault(questionId, Map.of()));
            }
        });

        return detailInfoList;
    }
}
