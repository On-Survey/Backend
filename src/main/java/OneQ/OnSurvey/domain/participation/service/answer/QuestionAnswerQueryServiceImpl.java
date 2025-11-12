package OneQ.OnSurvey.domain.participation.service.answer;

import OneQ.OnSurvey.domain.member.service.MemberFinder;
import OneQ.OnSurvey.domain.participation.entity.QuestionAnswer;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;
import OneQ.OnSurvey.domain.participation.repository.answer.AnswerRepository;
import OneQ.OnSurvey.domain.survey.model.response.SurveyManagementDetailResponse;
import org.springframework.stereotype.Service;

import java.util.List;

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
        List<SurveyManagementDetailResponse.DetailInfo> detailInfoList,
        List<Long> questionIdList, Long memberId
    ) {
        List<QuestionAnswer> answerList = answerRepository.getAnswersByQuestionIdListAndMemberId(questionIdList, memberId);

        // content 별로 묶어서 반환
        // TODO 구체화 필요
        return detailInfoList;
    }

}
