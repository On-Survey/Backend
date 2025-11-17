package OneQ.OnSurvey.domain.participation.repository.memberSurveyStatus;

import OneQ.OnSurvey.domain.participation.entity.MemberSurveyStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberSurveyStatusRepositoryImpl implements MemberSurveyStatusRepository {
    private final MemberSurveyStatusJpaRepository jpaRepository;

    @Override
    public MemberSurveyStatus save(MemberSurveyStatus memberSurveyStatus) {
        return jpaRepository.save(memberSurveyStatus);
    }
}
