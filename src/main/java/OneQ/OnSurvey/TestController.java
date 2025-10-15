package OneQ.OnSurvey;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test/fail")
    public String testFail() {
        return "ok";
    }

    @GetMapping("/test/ok")
    public String testOk() {
        return "ok";
    }
}
