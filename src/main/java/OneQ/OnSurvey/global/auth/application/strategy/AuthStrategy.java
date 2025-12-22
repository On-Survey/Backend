package OneQ.OnSurvey.global.auth.application.strategy;

import OneQ.OnSurvey.domain.member.Member;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthStrategy {
    Member authenticate(HttpServletRequest request);
}
