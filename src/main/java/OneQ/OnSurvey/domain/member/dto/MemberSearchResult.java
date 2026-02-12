package OneQ.OnSurvey.domain.member.dto;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.value.MemberStatus;
import OneQ.OnSurvey.domain.survey.model.Gender;

import java.util.List;

public record MemberSearchResult(
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
    public static MemberSearchResult from(Member member) {
        return new MemberSearchResult(
            member.getId(),
            member.getUserKey(),
            member.getName(),
            member.getEmail(),
            member.getPhoneNumber(),
            member.getBirthDay(),
            member.getGender(),
            member.getStatus(),
            member.getCoin()
        );
    }

    public static List<MemberSearchResult> from(List<Member> members) {
        return members.stream()
            .map(MemberSearchResult::from)
            .toList();
    }
}
