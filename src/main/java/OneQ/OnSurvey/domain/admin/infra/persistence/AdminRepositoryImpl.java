package OneQ.OnSurvey.domain.admin.infra.persistence;

import OneQ.OnSurvey.domain.admin.domain.model.Admin;
import OneQ.OnSurvey.domain.admin.domain.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminRepositoryImpl implements AdminRepository {

    private final AdminJpaRepository adminJpaRepository;

    @Override
    public Admin save(Admin save) {
        return adminJpaRepository.save(save);
    }

    @Override
    public Admin findByUsername(String username) {
        return adminJpaRepository.findAdminByUsername(username);
    }

    @Override
    public Admin findByUsernameWithAdminId(String adminId, String username) {
        return adminJpaRepository.findAdminByAdminIdAndUsername(adminId, username);
    }
}
