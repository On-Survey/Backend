package OneQ.OnSurvey.global.push.adapter.out.persistence;

import OneQ.OnSurvey.global.push.application.port.out.PushPropertyRepository;
import com.querydsl.core.group.GroupBy;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Map;

import static OneQ.OnSurvey.global.push.domain.entity.QPushProperty.pushProperty;

@Repository
@RequiredArgsConstructor
public class PushPropertyRepositoryImpl implements PushPropertyRepository {

    private final PushPropertyJpaRepository pushPropertyJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Map<String, String> findPushTemplateContextByCode(String code) {
        return jpaQueryFactory
            .from(pushProperty)
            .where(pushProperty.templateSetCode.eq(code))
            .transform(GroupBy.groupBy(pushProperty.key).as(pushProperty.value));
    }
}
