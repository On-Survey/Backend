package OneQ.OnSurvey.domain.participation.service.answer;

import OneQ.OnSurvey.domain.participation.entity.QuestionAnswer;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerStats;
import OneQ.OnSurvey.domain.participation.repository.answer.AnswerRepository;
import OneQ.OnSurvey.domain.survey.model.response.SurveyManagementDetailResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QuestionAnswerQueryServiceImpl extends AnswerQueryService<QuestionAnswer> {
    public QuestionAnswerQueryServiceImpl(
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

    public List<SurveyManagementDetailResponse.DetailInfo> getDetailInfo(
        List<SurveyManagementDetailResponse.DetailInfo> detailInfoList
    ) {

        Map<Boolean, List<SurveyManagementDetailResponse.DetailInfo>> typeInfoMap = detailInfoList.stream()
            .collect(Collectors.partitioningBy(detailInfo -> detailInfo.getType().isText()));

        List<Long> nonTextQuestionIdList = typeInfoMap.get(false).stream()
            .map(SurveyManagementDetailResponse.DetailInfo::getQuestionId)
            .toList();

        List<Long> textQuestionIdList = typeInfoMap.get(true).stream()
            .map(SurveyManagementDetailResponse.DetailInfo::getQuestionId)
            .toList();

        List<AnswerStats> nonTextAnswerStats = answerRepository.getAggregatedAnswersByQuestionIds(nonTextQuestionIdList);
        List<AnswerStats> textAnswerStats = answerRepository.getAnswersByQuestionIds(textQuestionIdList);

        Map<Long, Map<String, Integer>> nonTextAnswerMap = nonTextAnswerStats.stream()
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
                detailInfo.setAnswerList(textAnswerMap.get(questionId));
            } else {
                detailInfo.setAnswerMap(nonTextAnswerMap.get(questionId));
            }
        });

        return detailInfoList;
    }
}
