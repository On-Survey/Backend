package OneQ.OnSurvey.global.push.application;

import OneQ.OnSurvey.global.common.exception.CustomException;
import OneQ.OnSurvey.global.infra.toss.common.dto.push.PushTemplateAddRequest;
import OneQ.OnSurvey.global.infra.toss.common.dto.push.PushTemplateModifyRequest;
import OneQ.OnSurvey.global.infra.toss.common.dto.push.PushTemplateSendRequest;
import OneQ.OnSurvey.global.infra.toss.common.exception.TossErrorCode;
import OneQ.OnSurvey.global.push.application.port.in.PushUseCase;
import OneQ.OnSurvey.global.push.application.port.out.PushPropertyRepository;
import OneQ.OnSurvey.global.push.application.port.out.TossPushPort;
import OneQ.OnSurvey.global.push.domain.entity.PushProperty;
import OneQ.OnSurvey.global.push.domain.vo.PushTemplateAddVO;
import OneQ.OnSurvey.global.push.domain.vo.PushTemplateModifyVO;
import OneQ.OnSurvey.global.push.domain.vo.PushTemplateVO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PushFacade implements PushUseCase {

    private final TossPushPort tossPushPort;
    private final PushPropertyRepository pushPropertyRepository;

    @Value("${toss.secret.private-key}")
    private String privateKey;

    @Value("${toss.secret.public-crt}")
    private String publicCrt;

    private SSLContext sslContext;

    @PostConstruct
    public void init() throws Exception {
        this.sslContext = tossPushPort.createSSLContext(publicCrt, privateKey);
    }

    /**
     * 템플릿 이름으로 템플릿 코드, 기본 컨텍스트를 조회하고 인자로 받은 컨텍스트를 오버라이드하여 토스 푸시 API를 비동기로 호출하는 메서드
     * @param command 사용자 키, 템플릿 이름, 템플릿 컨텍스트를 포함하는 커맨드 객체
     */
    @Override
    public void fillTemplateAndSendPush(PushCommand command) {
        long userKey = command.userKey();
        Map<String, String> templateCtx = command.templateCtx();

        PushTemplateVO pushProperties = pushPropertyRepository.findPushTemplateContextByName(command.templateName());
        if (pushProperties == null) {
            throw new CustomException(TossErrorCode.TOSS_PUSH_NOT_FOUND);
        }

        // DB에 저장된 컨텍스트 기본값과 전달받은 컨텍스트 값을 병합하여 최종 컨텍스트 생성
        if (pushProperties.context() != null && !pushProperties.context().isEmpty()) {
            pushProperties.context().remove(null);

            if (command.templateCtx() != null && !command.templateCtx().isEmpty()) {
                pushProperties.context().keySet()
                    .forEach(
                        key -> pushProperties.context().put(
                            key,
                            templateCtx.getOrDefault(key, pushProperties.context().get(key))
                        )
                    );
            }
        }
        PushTemplateSendRequest request = PushTemplateSendRequest.builder()
            .userKey(userKey)
            .templateSetCode(pushProperties.code())
            .templateCtx(pushProperties.context())
            .build();

        try {
            tossPushPort.sendPush(sslContext, request);
        } catch (IOException e) {
            throw new CustomException(TossErrorCode.TOSS_PUSH_SEND_ERROR);
        }
    }

    @Override
    @Transactional
    public void addPushTemplate(PushTemplateAddRequest request) {
        PushTemplateAddVO vo = PushTemplateAddVO.of(request);

        List<PushProperty> propertyList = vo.addTemplateList().stream().map(
                addTemplate -> PushProperty.of(
                    addTemplate.name(),
                    addTemplate.code(),
                    addTemplate.contextKey(),
                    addTemplate.contextValue(),
                    addTemplate.description()
                )
            )
            .toList();

        pushPropertyRepository.saveAll(propertyList);
    }

    @Override
    @Transactional
    public void modifyTemplateContext(PushTemplateModifyRequest request) {
        PushTemplateModifyVO vo = PushTemplateModifyVO.of(request);

        Map<String, PushProperty> pushPropertyMap = pushPropertyRepository.findPushPropertiesByCode(vo.code())
            .stream()
            .collect(Collectors.toMap(PushProperty::getContextKey, Function.identity()));

        vo.modifyTemplateList().forEach(
            modify -> {
                PushProperty property = pushPropertyMap.get(modify.contextKey());
                if (property == null) {
                    return;
                }
                property.updateContext(modify.contextValue(), modify.description());
            }
        );

        pushPropertyRepository.saveAll(pushPropertyMap.values());
    }
}
