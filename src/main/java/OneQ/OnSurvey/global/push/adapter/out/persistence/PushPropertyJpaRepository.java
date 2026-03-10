package OneQ.OnSurvey.global.push.adapter.out.persistence;

import OneQ.OnSurvey.global.push.domain.entity.PushProperty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PushPropertyJpaRepository extends JpaRepository<PushProperty, Long> {
    List<PushProperty> findAllByTemplateSetCode(String templateSetCode);
}
