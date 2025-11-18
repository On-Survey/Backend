package OneQ.OnSurvey.domain.participation.repository.memberSurveyStatus;

import java.util.List;

import OneQ.OnSurvey.domain.participation.entity.MemberSurveyStatus;

public interface MemberSurveyStatusRepository {
    List<Long> getExcludedSurveyIdList(Long memberId, boolean checkScreened);

    MemberSurveyStatus save(MemberSurveyStatus memberSurveyStatus);
}
