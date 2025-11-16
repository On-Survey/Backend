package OneQ.OnSurvey.domain.participation.service.answer;

import OneQ.OnSurvey.domain.participation.entity.ScreeningAnswer;
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
public class ScreeningAnswerQueryService extends AnswerQueryService<ScreeningAnswer> {
    public ScreeningAnswerQueryService(
        AnswerRepository<ScreeningAnswer> answerRepository
    ) {
        super(answerRepository);
    }

    @Override
    public ScreeningAnswer getAnswerById(Long id, Long memberId) {
        return answerRepository.getAnswerByQuestionIdAndMemberId(id, memberId);
    }

    @Override
    public ScreeningAnswer createAnswerFromDto(AnswerInsertDto.AnswerInfo answerInfo) {
        return ScreeningAnswer.from(answerInfo);
    }

    @Override
    public List<SurveyManagementDetailResponse.DetailInfo> getDetailInfo(
        Long surveyId,
        List<SurveyManagementDetailResponse.DetailInfo> detailInfoList
    ) {
        log.info("[SCREENING_ANSWER_SERVICE] 스크리닝 별 응답결과 조회 - surveyId: {}", surveyId);

        List<Long> screeningIdList = detailInfoList.stream()
            .map(SurveyManagementDetailResponse.DetailInfo::getQuestionId)
            .toList();

        log.info("[SCREENING_ANSWER_SERVICE] 응답을 조회할 스크리닝 IDs - screeningIds: {}", screeningIdList);

        List<AnswerStats> screeningAnswerStats = answerRepository.getAggregatedAnswersByQuestionIds(screeningIdList);

        Map<Long, Map<String, Long>> screeningAnswerMap = screeningAnswerStats.stream()
            .collect(Collectors.groupingBy(
                AnswerStats::getQuestionId,
                Collectors.toMap(
                    AnswerStats::getContent,
                    AnswerStats::getCount
                )
            ));

        detailInfoList.forEach(detailInfo -> {
            Long questionId = detailInfo.getQuestionId();
            detailInfo.setAnswerMap(screeningAnswerMap.getOrDefault(questionId, Map.of()));
        });

        return detailInfoList;
    }
}
