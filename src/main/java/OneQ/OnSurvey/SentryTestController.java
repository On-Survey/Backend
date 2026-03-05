package OneQ.OnSurvey;

import io.sentry.Sentry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SentryTestController {

    // 자동 캡처: 예외를 던지면 Sentry Starter가 자동 보고
    @GetMapping("/sentry-test/throw")
    public String throwError() {
        throw new RuntimeException("Sentry auto-capture test");
    }

    // 수동 캡처: try-catch 안에서 직접 보고
    @GetMapping("/sentry-test/capture")
    public String captureError() {
        try {
            throw new RuntimeException("Sentry manual-capture test");
        } catch (Exception e) {
            Sentry.captureException(e);
        }
        return "ok";
    }
}