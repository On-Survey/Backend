package OneQ.OnSurvey.domain.participation.service.answer;

import OneQ.OnSurvey.domain.participation.entity.ScreeningAnswer;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerStats;
import OneQ.OnSurvey.domain.participation.repository.answer.AnswerRepository;
import OneQ.OnSurvey.domain.survey.model.response.SurveyManagementDetailResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        List<SurveyManagementDetailResponse.DetailInfo> detailInfoList
    ) {
        List<Long> screeningIdList = detailInfoList.stream()
            .map(SurveyManagementDetailResponse.DetailInfo::getQuestionId)
            .toList();

        List<AnswerStats> screeningAnswerStats = answerRepository.getAggregatedAnswersByQuestionIds(screeningIdList);

        Map<Long, Map<String, Integer>> screeningAnswerMap = screeningAnswerStats.stream()
            .collect(Collectors.groupingBy(
                AnswerStats::getQuestionId,
                Collectors.toMap(
                    AnswerStats::getContent,
                    AnswerStats::getCount
                )
            ));

        detailInfoList.forEach(detailInfo -> {
            Long questionId = detailInfo.getQuestionId();
            detailInfo.setAnswerMap(screeningAnswerMap.get(questionId));
        });

        return detailInfoList;
    }
}
