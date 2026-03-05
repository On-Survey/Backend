package OneQ.OnSurvey.global.auth.application.strategy;

import OneQ.OnSurvey.domain.member.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthStrategy {
    Member authenticate(HttpServletRequest request, HttpServletResponse response);
}
