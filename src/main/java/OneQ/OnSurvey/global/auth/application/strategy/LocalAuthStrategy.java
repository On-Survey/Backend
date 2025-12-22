package OneQ.OnSurvey.global.auth.application.strategy;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.value.Role;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Profile({"local", "docker-local"})
@Component
@NoArgsConstructor
public class LocalAuthStrategy implements AuthStrategy {

    static {
        log.info("===== LocalAuthStrategy authenticate called =====");
    }

    @Override
    public Member authenticate(HttpServletRequest request) {
        return Member.builder()
            .id(1L)
            .userKey(1234567890L)
            .role(Role.ROLE_MEMBER)
            .build();
    }
}
