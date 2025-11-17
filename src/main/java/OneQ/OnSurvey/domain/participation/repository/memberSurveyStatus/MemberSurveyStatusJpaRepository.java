package OneQ.OnSurvey.domain.participation.repository.memberSurveyStatus;

import OneQ.OnSurvey.domain.participation.entity.MemberSurveyStatus;
import OneQ.OnSurvey.domain.participation.entity.id.MemberSurveyStatusId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberSurveyStatusJpaRepository extends JpaRepository<MemberSurveyStatus, MemberSurveyStatusId> {
}
