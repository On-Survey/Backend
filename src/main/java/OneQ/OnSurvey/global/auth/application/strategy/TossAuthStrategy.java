package OneQ.OnSurvey.global.auth.application.strategy;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.repository.MemberRepository;
import OneQ.OnSurvey.global.auth.application.AuthUseCase;
import OneQ.OnSurvey.global.infra.toss.common.dto.auth.LoginMeResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

@Slf4j
@Profile({"prod", "dev"})
@Component
@RequiredArgsConstructor
public class TossAuthStrategy implements AuthStrategy {

    private final AuthUseCase authUseCase;
    private final MemberRepository memberRepository;

    static {
        log.info("===== ProdAuthStrategy authenticate called =====");
    }

    @Override
    public Member authenticate(HttpServletRequest request, HttpServletResponse response) {
        LoginMeResponse.Success me = authUseCase.authenticateWithToss(request, response);
        return memberRepository.findMemberByUserKey(me.userKey())
            .orElseThrow(() -> new BadCredentialsException("member not found"));
    }
}
