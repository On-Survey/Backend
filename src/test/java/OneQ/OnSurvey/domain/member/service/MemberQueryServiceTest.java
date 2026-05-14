package OneQ.OnSurvey.domain.member.service;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.MemberErrorCode;
import OneQ.OnSurvey.domain.member.dto.MemberInfoResponse;
import OneQ.OnSurvey.domain.member.dto.MemberSearchResult;
import OneQ.OnSurvey.domain.member.repository.MemberRepository;
import OneQ.OnSurvey.domain.member.value.MemberStatus;
import OneQ.OnSurvey.domain.member.value.Role;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.global.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberQueryServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberQueryService memberQueryService;

    private Member buildMember(Long userKey) {
        return Member.createMember(
                userKey, "홍길동", "010-1234-5678",
                "19900101", "test@test.com",
                Gender.MALE, Role.ROLE_MEMBER, MemberStatus.ACTIVE
        );
    }

    @Test
    @DisplayName("getMemberByUserKey - 멤버 존재 시 반환")
    void getMemberByUserKey_found_returnsMember() {
        Member member = buildMember(1000L);
        given(memberRepository.findMemberByUserKey(1000L)).willReturn(Optional.of(member));

        Member result = memberQueryService.getMemberByUserKey(1000L);

        assertThat(result).isEqualTo(member);
        assertThat(result.getUserKey()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("getMemberByUserKey - 멤버 없으면 MEMBER_NOT_FOUND 예외")
    void getMemberByUserKey_notFound_throwsException() {
        given(memberRepository.findMemberByUserKey(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberQueryService.getMemberByUserKey(999L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    @Test
    @DisplayName("getMemberInfo - 멤버 존재 시 MemberInfoResponse 반환")
    void getMemberInfo_found_returnsResponse() {
        Member member = Member.builder()
                .userKey(2000L)
                .name("김철수")
                .profileUrl("https://example.com/img.jpg")
                .coin(500L)
                .promotionPoint(100L)
                .onboardingCompleted(true)
                .role(Role.ROLE_MEMBER)
                .status(MemberStatus.ACTIVE)
                .build();
        given(memberRepository.findMemberByUserKey(2000L)).willReturn(Optional.of(member));

        MemberInfoResponse response = memberQueryService.getMemberInfo(2000L);

        assertThat(response.name()).isEqualTo("김철수");
        assertThat(response.profileUrl()).isEqualTo("https://example.com/img.jpg");
        assertThat(response.coin()).isEqualTo(500L);
        assertThat(response.promotionPoint()).isEqualTo(100L);
        assertThat(response.isOnboardingCompleted()).isTrue();
    }

    @Test
    @DisplayName("getMemberInfo - 멤버 없으면 MEMBER_NOT_FOUND 예외")
    void getMemberInfo_notFound_throwsException() {
        given(memberRepository.findMemberByUserKey(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberQueryService.getMemberInfo(999L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    @Test
    @DisplayName("validateAdminRoleAndGetMemberIdByUserKey - repository 결과 그대로 반환")
    void validateAdminRoleAndGetMemberIdByUserKey_delegatesToRepository() {
        given(memberRepository.validateAdminRoleAndGetMemberIdByUserKey(3000L)).willReturn(42L);

        Long result = memberQueryService.validateAdminRoleAndGetMemberIdByUserKey(3000L);

        assertThat(result).isEqualTo(42L);
        verify(memberRepository).validateAdminRoleAndGetMemberIdByUserKey(3000L);
    }

    @Test
    @DisplayName("searchMembers - 결과 목록을 MemberSearchResult로 변환")
    void searchMembers_returnsMappedList() {
        Member m1 = buildMember(1001L);
        Member m2 = buildMember(1002L);
        given(memberRepository.searchMembers("test@test.com", null, null, null))
                .willReturn(List.of(m1, m2));

        List<MemberSearchResult> results = memberQueryService.searchMembers("test@test.com", null, null, null);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).userKey()).isEqualTo(1001L);
        assertThat(results.get(1).userKey()).isEqualTo(1002L);
    }

    @Test
    @DisplayName("searchMembers - 결과 없으면 빈 리스트 반환")
    void searchMembers_emptyResult_returnsEmptyList() {
        given(memberRepository.searchMembers(null, null, null, "없는이름"))
                .willReturn(List.of());

        List<MemberSearchResult> results = memberQueryService.searchMembers(null, null, null, "없는이름");

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("getUsernameByUserKey - repository 결과 그대로 반환")
    void getUsernameByUserKey_delegatesToRepository() {
        given(memberRepository.getUsernameByUserKey(5000L)).willReturn("홍길동");

        String result = memberQueryService.getUsernameByUserKey(5000L);

        assertThat(result).isEqualTo("홍길동");
        verify(memberRepository).getUsernameByUserKey(5000L);
    }
}
