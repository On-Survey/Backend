package OneQ.OnSurvey.global.auth.application;

import OneQ.OnSurvey.global.auth.dto.AdminLoginRequest;
import OneQ.OnSurvey.global.auth.dto.AdminLoginResult;
import OneQ.OnSurvey.global.auth.dto.AdminRegisterRequest;
import OneQ.OnSurvey.global.auth.dto.AdminSessionInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 어드민 세션 기반 인증 UseCase
 */
public interface AdminSessionUseCase {

    /**
     * 어드민 로그인 처리
     * - Rate Limit 검증
     * - 자격 증명 확인
     * - 세션 생성 및 쿠키 발급
     *
     * @param request 로그인 요청 정보
     * @param httpRequest HTTP 요청
     * @param httpResponse HTTP 응답
     * @return 로그인 결과
     */
    AdminLoginResult login(AdminLoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    /**
     * 어드민 등록
     *
     * @param request 등록 요청 정보
     * @return 등록 성공 여부
     */
    boolean register(AdminRegisterRequest request);

    /**
     * 현재 세션의 어드민 정보 조회 및 세션 갱신
     *
     * @param adminId 어드민 ID
     * @param username 어드민 사용자명
     * @param httpRequest HTTP 요청
     * @return 어드민 세션 정보
     */
    AdminSessionInfo refreshSession(String adminId, String username, HttpServletRequest httpRequest);
}
