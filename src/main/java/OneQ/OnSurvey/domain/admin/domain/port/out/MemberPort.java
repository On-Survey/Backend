package OneQ.OnSurvey.domain.admin.domain.port.out;

public interface MemberPort {

    Long validateAdminRoleAndGetMemberIdByUserKey(Long userKey);
}
