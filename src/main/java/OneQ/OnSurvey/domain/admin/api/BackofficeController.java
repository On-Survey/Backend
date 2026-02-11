package OneQ.OnSurvey.domain.admin.api;

import OneQ.OnSurvey.domain.admin.api.dto.response.AuthRegisterResponse;
import OneQ.OnSurvey.global.auth.application.AdminSessionUseCase;
import OneQ.OnSurvey.global.auth.dto.AdminLoginRequest;
import OneQ.OnSurvey.global.auth.dto.AdminRegisterRequest;
import OneQ.OnSurvey.global.common.response.SuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping("/v1/bo")
@RequiredArgsConstructor
public class BackofficeController {

    private final AdminSessionUseCase adminSessionUseCase;

    private static final String ADMIN_SESSION_USERNAME = "ADMIN_USERNAME";

    @PostMapping("/auth/login")
    @ResponseBody
    public SuccessResponse<Void> backofficeLogin(
        @RequestBody AdminLoginRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
    ) {
        adminSessionUseCase.login(request, httpRequest, httpResponse);
        return SuccessResponse.ok(null);
    }

    @PostMapping("/auth/register")
    @ResponseBody
    public SuccessResponse<AuthRegisterResponse> backofficeRegister(
        @RequestBody AdminRegisterRequest request
    ) {
        adminSessionUseCase.register(request);
        return SuccessResponse.ok(new AuthRegisterResponse(request.username(), request.name()));
    }

    /**
     * 로그아웃 처리
     */
    @PostMapping("/auth/logout")
    @ResponseBody
    public SuccessResponse<Void> logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            String username = (String) session.getAttribute(ADMIN_SESSION_USERNAME);
            log.info("[백오피스 로그아웃] username: {}", username);
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return SuccessResponse.ok(null);
    }

    @GetMapping()
    public String backoffice() {
        return "bo/login";
    }

    @GetMapping("/index")
    public String index(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String adminUsername = (String) session.getAttribute(ADMIN_SESSION_USERNAME);
            model.addAttribute("adminName", adminUsername);
        } else {
            model.addAttribute("adminName", "Admin");
        }
        return "bo/index";
    }

    @GetMapping("/survey")
    public String survey() {
        return "bo/survey";
    }

    @GetMapping("/form-request")
    public String formRequest() {
        return "bo/form-request";
    }

    @GetMapping("/member-search")
    public String memberSearch() {
        return "bo/member-search";
    }
}
