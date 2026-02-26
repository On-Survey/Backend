package OneQ.OnSurvey.global.push.adapter.out.persistence;

import OneQ.OnSurvey.global.push.domain.entity.PushProperty;
import OneQ.OnSurvey.global.push.domain.entity.PushPropertyId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushPropertyJpaRepository extends JpaRepository<PushProperty, PushPropertyId> {
}
