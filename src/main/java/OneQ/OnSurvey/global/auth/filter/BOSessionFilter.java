package OneQ.OnSurvey.global.auth.filter;

import OneQ.OnSurvey.domain.member.value.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class BackofficeSessionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (!isPublicPath(path)) {
            HttpSession session = request.getSession(false);

            if (session != null && session.getAttribute("ADMIN_ID") != null) {
                Long adminId = (Long) session.getAttribute("ADMIN_ID");
                String username = (String) session.getAttribute("ADMIN_USERNAME");

                // SecurityContext에 ADMIN 권한 설정
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        adminId,
                        null,
                        List.of(new SimpleGrantedAuthority(Role.ROLE_ADMIN.name()))
                    );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return path.equals("/v1/bo") || path.equals("/v1/bo/admin/login");
    }
}