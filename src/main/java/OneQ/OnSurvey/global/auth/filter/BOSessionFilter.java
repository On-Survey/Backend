package OneQ.OnSurvey.global.auth.filter;

import OneQ.OnSurvey.domain.admin.domain.model.Admin;
import OneQ.OnSurvey.domain.admin.domain.repository.AdminRepository;
import OneQ.OnSurvey.global.auth.custom.CustomAdminDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class BOSessionFilter extends OncePerRequestFilter {

    private final AdminRepository adminRepository;

    private static final String ADMIN_SESSION_KEY = "ADMIN_ID";
    private static final String ADMIN_SESSION_USERNAME = "ADMIN_USERNAME";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String path = req.getRequestURI();
        String ctx  = req.getContextPath();
        if (ctx != null && !ctx.isEmpty() && path.startsWith(ctx)) {
            path = path.substring(ctx.length());
        }

        return "OPTIONS".equalsIgnoreCase(req.getMethod())
            || path.endsWith(".css")
            || path.endsWith(".js")
            || path.endsWith(".ico")
            || path.endsWith(".png")
            || path.endsWith(".jpg")
            || path.equals("/v1/bo")
            || path.equals("/v1/bo/auth/login");
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute(ADMIN_SESSION_KEY) != null) {
            String adminId = (String) session.getAttribute(ADMIN_SESSION_KEY);
            String username = (String) session.getAttribute(ADMIN_SESSION_USERNAME);

            Admin admin = adminRepository.findByUsernameWithAdminId(adminId, username);
            if (admin == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // SecurityContext에 ADMIN 권한 설정
            CustomAdminDetails principal = new CustomAdminDetails(admin);
            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                    principal, null, principal.getAuthorities()
                );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}