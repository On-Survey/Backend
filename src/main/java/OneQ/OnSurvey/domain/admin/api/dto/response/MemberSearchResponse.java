package OneQ.OnSurvey.domain.admin.api.dto.response;

import OneQ.OnSurvey.domain.admin.domain.model.member.AdminMemberView;

import java.util.List;

public record MemberSearchResponse(
    List<MemberSearchInfo> members
) {
    public record MemberSearchInfo(
        Long id,
        Long userKey,
        String name,
        String email,
        String phoneNumber,
        String birthDay,
        String gender,
        String status,
        Long coin
    ) {
        public static MemberSearchInfo from(AdminMemberView result) {
            return new MemberSearchInfo(
                result.id(),
                result.userKey(),
                result.name(),
                result.email(),
                result.phoneNumber(),
                result.birthDay(),
                result.gender(),
                result.status(),
                result.coin()
            );
        }
    }

    public static MemberSearchResponse from(List<AdminMemberView> results) {
        return new MemberSearchResponse(
            results.stream()
                .map(MemberSearchInfo::from)
                .toList()
        );
    }
}
