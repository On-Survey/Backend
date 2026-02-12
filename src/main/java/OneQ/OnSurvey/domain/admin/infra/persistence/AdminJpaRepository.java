package OneQ.OnSurvey.domain.admin.infra.persistence;

import OneQ.OnSurvey.domain.admin.domain.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminJpaRepository extends JpaRepository<Admin, Long> {
    Admin findAdminByUsername(String username);
    Admin findAdminByAdminIdAndUsername(String adminId, String username);
}
