package OneQ.OnSurvey.global.infra.toss.api.auth;

import OneQ.OnSurvey.global.auth.application.AuthUseCase;
import OneQ.OnSurvey.global.infra.toss.auth.TossUnlinkValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/connect-out")
@RequiredArgsConstructor
@Slf4j
public class UnlinkCallbackController {

    private final AuthUseCase authUseCase;

    @GetMapping
    public ResponseEntity<Boolean> unlink(
            @RequestParam("userKey") Long userKey,
            @RequestParam("referrer") String referrer
    ) {
        log.info("[서비스 끊기 신청] : userKey=" + userKey + "referrer=" + referrer);
        TossUnlinkValue unlinkValue = TossUnlinkValue.valueOf(referrer);
        authUseCase.unlink(userKey, unlinkValue);
        return ResponseEntity.ok(true);
    }
}

