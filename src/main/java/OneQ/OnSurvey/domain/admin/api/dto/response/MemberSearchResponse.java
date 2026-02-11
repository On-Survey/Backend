package OneQ.OnSurvey.domain.admin.api.dto.response;

import OneQ.OnSurvey.domain.member.dto.MemberSearchResult;
import OneQ.OnSurvey.domain.member.value.MemberStatus;
import OneQ.OnSurvey.domain.survey.model.Gender;

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
        Gender gender,
        MemberStatus status,
        Long coin
    ) {
        public static MemberSearchInfo from(MemberSearchResult result) {
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

    public static MemberSearchResponse from(List<MemberSearchResult> results) {
        return new MemberSearchResponse(
            results.stream()
                .map(MemberSearchInfo::from)
                .toList()
        );
    }
}
