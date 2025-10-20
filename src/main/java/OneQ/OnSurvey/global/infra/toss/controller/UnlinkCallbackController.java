package OneQ.OnSurvey.global.infra.toss.controller;

import OneQ.OnSurvey.global.infra.toss.TossUnlinkValue;
import OneQ.OnSurvey.global.infra.toss.service.TossUnlinkService;
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

    private final TossUnlinkService tossUnlinkService;

    @GetMapping
    public ResponseEntity<Boolean> unlink(
            @RequestParam("userKey") Long userKey,
            @RequestParam("referrer") String referrer
    ) {
        log.info("[서비스 끊기 신청] : userKey=" + userKey + "referrer=" + referrer);
        TossUnlinkValue unlinkValue = TossUnlinkValue.valueOf(referrer);
        tossUnlinkService.unlink(userKey, unlinkValue);
        return ResponseEntity.ok(true);
    }
}

