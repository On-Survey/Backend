package OneQ.OnSurvey.global.push.adapter.in;

import OneQ.OnSurvey.global.common.event.pushAlim.PushAlimEvent;
import OneQ.OnSurvey.global.push.application.port.in.PushUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushEventListener {

    private final PushUseCase pushUseCase;

    @Async("pushAlimExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePushAlimEvent(PushAlimEvent event) {
        long userKey = event.getTargetUserKey();
        Map<String, String> templateCtx = event.getPushContext();

        PushUseCase.PushCommand command = new PushUseCase.PushCommand(
            userKey,
            event.getPushTemplateName(),
            templateCtx
        );

        pushUseCase.fillTemplateAndSendPush(command);
    }
}
