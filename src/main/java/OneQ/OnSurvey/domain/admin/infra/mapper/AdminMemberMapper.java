package OneQ.OnSurvey.domain.admin.infra.mapper;

import OneQ.OnSurvey.domain.admin.domain.model.member.AdminMemberView;
import OneQ.OnSurvey.domain.member.dto.MemberSearchResult;

public final class AdminMemberMapper {

    public static AdminMemberView toAdminMemberView(MemberSearchResult memberSearchResult) {
        return new AdminMemberView(
            memberSearchResult.id(),
            memberSearchResult.userKey(),
            memberSearchResult.name(),
            memberSearchResult.email(),
            memberSearchResult.phoneNumber(),
            memberSearchResult.birthDay(),
            memberSearchResult.gender() != null ? memberSearchResult.gender().name() : null,
            memberSearchResult.status() != null ? memberSearchResult.status().name() : null,
            memberSearchResult.coin()
        );
    }
}
