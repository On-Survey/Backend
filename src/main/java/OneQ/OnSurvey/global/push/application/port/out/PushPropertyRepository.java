package OneQ.OnSurvey.global.push.application.port.out;

import OneQ.OnSurvey.global.push.domain.entity.PushProperty;
import OneQ.OnSurvey.global.push.domain.vo.PushTemplateVO;

import java.util.Collection;
import java.util.List;

public interface PushPropertyRepository {

    List<PushProperty> findPushPropertiesByCode(String code);

    PushTemplateVO findPushTemplateContextByName(String name);

    void saveAll(Collection<PushProperty> pushProperties);
}
