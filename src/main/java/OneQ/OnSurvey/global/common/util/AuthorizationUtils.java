package OneQ.OnSurvey.global.common.util;

import OneQ.OnSurvey.domain.member.value.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationUtils {

    public static boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(auth -> auth.equals(Role.ROLE_ADMIN.name()));
    }

    public static boolean validateOwnershipOrAdmin(Long resourceOwnerId, Long currentUserId) {
        return !(isAdmin() || !resourceOwnerId.equals(currentUserId));
    }
}