package OneQ.OnSurvey.global.push.adapter.out.persistence;

import OneQ.OnSurvey.global.push.application.port.out.PushPropertyRepository;
import OneQ.OnSurvey.global.push.domain.entity.PushProperty;
import OneQ.OnSurvey.global.push.domain.vo.PushTemplateVO;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

import static OneQ.OnSurvey.global.push.domain.entity.QPushProperty.pushProperty;

@Repository
@RequiredArgsConstructor
public class PushPropertyRepositoryImpl implements PushPropertyRepository {

    private final PushPropertyJpaRepository pushPropertyJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<PushProperty> findPushPropertiesByCode(String code) {
        return pushPropertyJpaRepository.findAllByTemplateSetCode(code);
    }

    @Override
    public PushTemplateVO findPushTemplateContextByName(String name) {
        return jpaQueryFactory
            .from(pushProperty)
            .where(pushProperty.templateName.eq(name))
            .transform(
                GroupBy.groupBy(pushProperty.templateName).as(
                    Projections.constructor(PushTemplateVO.class,
                        pushProperty.templateSetCode,
                        GroupBy.map(pushProperty.contextKey, pushProperty.defaultValue)
                    )
                )
            )
            .getOrDefault(name, null);
    }

    @Override
    public void saveAll(Collection<PushProperty> pushProperties) {
        pushPropertyJpaRepository.saveAllAndFlush(pushProperties);
    }
}
