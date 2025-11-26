package OneQ.OnSurvey.domain.participation.service.answer;

import OneQ.OnSurvey.domain.participation.entity.AbstractAnswer;
import OneQ.OnSurvey.domain.survey.model.SurveyResponseFilterCondition;
import OneQ.OnSurvey.domain.survey.model.response.SurveyManagementDetailResponse;

import java.util.List;

public interface AnswerQuery<E extends AbstractAnswer> {
    List<E> getAnswersByIdListAndMemberId(List<Long> idList, Long memberId);
    default List<SurveyManagementDetailResponse.DetailInfo> getDetailInfoByMemberId(
        List<SurveyManagementDetailResponse.DetailInfo> detailInfoList,
        List<Long> questionIdList, Long memberId) {
        return null;
    }
    default E getAnswerById(Long id, Long memberId) {
        return null;
    }

    List<SurveyManagementDetailResponse.DetailInfo> getDetailInfo(
            Long surveyId,
            List<SurveyManagementDetailResponse.DetailInfo> detailInfoList
    );

    /** 필터 버전 */
    default List<SurveyManagementDetailResponse.DetailInfo> getDetailInfo(
            Long surveyId,
            SurveyResponseFilterCondition filter,
            List<SurveyManagementDetailResponse.DetailInfo> detailInfoList
    ) {
        return getDetailInfo(surveyId, detailInfoList);
    }
}
