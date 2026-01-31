package OneQ.OnSurvey.domain.admin.domain.repository;

import OneQ.OnSurvey.domain.admin.domain.model.Admin;

public interface AdminRepository {

    Admin save(Admin admin);
    Admin findByUsername(String username);
    Admin findByUsernameWithAdminId(String username, String adminId);
}
