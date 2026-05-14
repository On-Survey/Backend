package OneQ.OnSurvey.domain.member.service;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.MemberErrorCode;
import OneQ.OnSurvey.domain.member.repository.MemberRepository;
import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.member.value.MemberStatus;
import OneQ.OnSurvey.domain.member.value.Role;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.Residence;
import OneQ.OnSurvey.global.auth.dto.DecryptedLoginMeResponse;
import OneQ.OnSurvey.global.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberModifyServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberModifyService memberModifyService;

    private Member buildMember(Long userKey) {
        return Member.createMember(
                userKey, "기존이름", "010-0000-0000",
                "19850101", "old@test.com",
                Gender.FEMALE, Role.ROLE_MEMBER, MemberStatus.ACTIVE
        );
    }

    private DecryptedLoginMeResponse buildLoginResponse(long userKey) {
        return new DecryptedLoginMeResponse(
                userKey, "scope", List.of("serviceAgreed"), "policy",
                "certTxId", "새이름", "010-9999-9999",
                "19900101", Gender.MALE, "KR", "new@test.com"
        );
    }

    @Test
    @DisplayName("upsertMember - 기존 멤버가 있으면 정보 업데이트")
    void upsertMember_existingMember_updatesFields() {
        Member existing = buildMember(1000L);
        DecryptedLoginMeResponse loginResponse = buildLoginResponse(1000L);
        given(memberRepository.findMemberByUserKey(1000L)).willReturn(Optional.of(existing));

        Member result = memberModifyService.upsertMember(loginResponse);

        assertThat(result.getName()).isEqualTo("새이름");
        assertThat(result.getPhoneNumber()).isEqualTo("010-9999-9999");
        assertThat(result.getEmail()).isEqualTo("new@test.com");
        assertThat(result.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("upsertMember - 신규 멤버면 생성 후 저장")
    void upsertMember_newMember_createsAndSaves() {
        DecryptedLoginMeResponse loginResponse = buildLoginResponse(2000L);
        given(memberRepository.findMemberByUserKey(2000L)).willReturn(Optional.empty());
        given(memberRepository.save(any(Member.class))).willAnswer(inv -> inv.getArgument(0));

        Member result = memberModifyService.upsertMember(loginResponse);

        assertThat(result.getUserKey()).isEqualTo(2000L);
        assertThat(result.getName()).isEqualTo("새이름");
        assertThat(result.getRole()).isEqualTo(Role.ROLE_MEMBER);
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("upsertMember - agreeTerms에 serviceAgreed 포함 시 서비스 동의 true")
    void upsertMember_withServiceAgreed_setsServiceAgreedTrue() {
        given(memberRepository.findMemberByUserKey(3000L)).willReturn(Optional.empty());
        given(memberRepository.save(any(Member.class))).willAnswer(inv -> inv.getArgument(0));

        DecryptedLoginMeResponse loginResponse = new DecryptedLoginMeResponse(
                3000L, "scope", List.of("serviceAgreed"),
                "policy", "certTxId", "이름", "010-1111-1111",
                "20000101", Gender.MALE, "KR", "email@test.com"
        );

        Member result = memberModifyService.upsertMember(loginResponse);

        assertThat(result.isServiceAgreed()).isTrue();
        assertThat(result.isMarketingAgreed()).isFalse();
    }

    @Test
    @DisplayName("upsertMember - agreeTerms가 null이면 동의 상태 변경 없음")
    void upsertMember_nullAgreeTerms_noChange() {
        given(memberRepository.findMemberByUserKey(4000L)).willReturn(Optional.empty());
        given(memberRepository.save(any(Member.class))).willAnswer(inv -> inv.getArgument(0));

        DecryptedLoginMeResponse loginResponse = new DecryptedLoginMeResponse(
                4000L, "scope", null,
                "policy", "certTxId", "이름", "010-2222-2222",
                "19950101", Gender.FEMALE, "KR", "email@test.com"
        );

        Member result = memberModifyService.upsertMember(loginResponse);

        assertThat(result.isServiceAgreed()).isFalse();
        assertThat(result.isMarketingAgreed()).isFalse();
    }

    @Test
    @DisplayName("changeMemberStatusTossConnectOut - TOSS_CONNECT_OUT으로 상태 변경 후 저장")
    void changeMemberStatusTossConnectOut_updatesStatusAndSaves() {
        Member member = buildMember(5000L);
        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);

        memberModifyService.changeMemberStatusTossConnectOut(member);

        verify(memberRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(MemberStatus.TOSS_CONNECT_OUT);
    }

    @Test
    @DisplayName("changeProfileImage - 멤버 존재 시 프로필 URL 변경")
    void changeProfileImage_found_changesProfileUrl() {
        Member member = buildMember(6000L);
        given(memberRepository.findMemberByUserKey(6000L)).willReturn(Optional.of(member));

        memberModifyService.changeProfileImage(6000L, "https://new-image.com/photo.jpg");

        assertThat(member.getProfileUrl()).isEqualTo("https://new-image.com/photo.jpg");
    }

    @Test
    @DisplayName("changeProfileImage - 멤버 없으면 MEMBER_NOT_FOUND 예외")
    void changeProfileImage_notFound_throwsException() {
        given(memberRepository.findMemberByUserKey(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberModifyService.changeProfileImage(999L, "url"))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    @Test
    @DisplayName("completeOnboarding - 멤버 존재 시 온보딩 완료 처리")
    void completeOnboarding_found_completesOnboarding() {
        Member member = buildMember(7000L);
        given(memberRepository.findMemberByUserKey(7000L)).willReturn(Optional.of(member));

        memberModifyService.completeOnboarding(
                7000L, Residence.SEOUL, Set.of(Interest.HEALTH, Interest.CULTURE)
        );

        assertThat(member.isOnboardingCompleted()).isTrue();
        assertThat(member.getResidence()).isEqualTo(Residence.SEOUL);
        assertThat(member.getInterests()).containsExactlyInAnyOrder(Interest.HEALTH, Interest.CULTURE);
    }

    @Test
    @DisplayName("completeOnboarding - 멤버 없으면 MEMBER_NOT_FOUND 예외")
    void completeOnboarding_notFound_throwsException() {
        given(memberRepository.findMemberByUserKey(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberModifyService.completeOnboarding(999L, Residence.SEOUL, Set.of()))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    @Test
    @DisplayName("deleteById - repository.deleteById 호출")
    void deleteById_delegatesToRepository() {
        memberModifyService.deleteById(8000L);

        verify(memberRepository).deleteById(8000L);
    }
}
