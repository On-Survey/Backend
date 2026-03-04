package OneQ.OnSurvey.domain.admin.application;

import OneQ.OnSurvey.domain.admin.domain.model.Admin;
import OneQ.OnSurvey.domain.admin.domain.model.AdminRole;
import OneQ.OnSurvey.domain.admin.domain.port.out.MemberPort;
import OneQ.OnSurvey.domain.admin.domain.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final MemberPort memberPort;
    private final PasswordEncoder passwordEncoder;

    public String authenticate(String username, String rawPassword) {

        Admin admin = adminRepository.findByUsername(username);
        if (admin == null || !admin.matchPassword(passwordEncoder, rawPassword)) {
            return null;
        }

        return admin.getAdminId();

    }

    @Transactional
    public boolean register(Long userKey, String username, String password, String name) {
        Admin existingAdmin = adminRepository.findByUsername(username);
        Long memberId = memberPort.validateAdminRoleAndGetMemberIdByUserKey(userKey);
        if (existingAdmin != null || memberId == null) {
            return false;
        }

        String encodedPassword = passwordEncoder.encode(password);

        Admin newAdmin = Admin.builder()
            .memberId(memberId)
            .userKey(userKey)
            .username(username)
            .password(encodedPassword)
            .name(name)
            .role(AdminRole.ROLE_ADMIN)
            .build();

        adminRepository.save(newAdmin);
        return true;
    }
}
