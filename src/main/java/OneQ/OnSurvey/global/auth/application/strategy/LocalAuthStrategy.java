package OneQ.OnSurvey.global.auth.application.strategy;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.value.MemberStatus;
import OneQ.OnSurvey.domain.member.value.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Profile({"local", "docker-local"})
@Component
@RequiredArgsConstructor
public class LocalAuthStrategy implements AuthStrategy {

    static {
        log.info("===== LocalAuthStrategy authenticate called =====");
    }

    @Override
    public Member authenticate(HttpServletRequest request, HttpServletResponse response) {
        return Member.builder()
            .id(2L)
            .userKey(22L)
            .role(Role.ROLE_MEMBER)
            .status(MemberStatus.ACTIVE)
            .build();
    }
}
