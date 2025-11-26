package OneQ.OnSurvey.domain.member;

import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.member.value.MemberStatus;
import OneQ.OnSurvey.domain.member.value.Role;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.Residence;
import OneQ.OnSurvey.global.entity.BaseEntity;
import OneQ.OnSurvey.global.exception.CustomException;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static OneQ.OnSurvey.domain.member.value.AgreeTerm.MARKETING_AGREED;
import static OneQ.OnSurvey.domain.member.value.AgreeTerm.SERVICE_AGREED;
import static OneQ.OnSurvey.domain.member.value.MemberStatus.TOSS_CONNECT_OUT;

@Entity
@Table(name = "MEMBER")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEMBER_ID")
    private Long id;

    private Long userKey;

    private String name;

    private String phoneNumber;

    private String birthDay;

    private String email;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    @Builder.Default
    private boolean serviceAgreed = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean marketingAgreed = false;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @Column(nullable = false)
    @Builder.Default
    private Long coin = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Long promotionPoint = 0L;

    private String profileUrl;

    @Column(nullable = false)
    @Builder.Default
    private boolean onboardingCompleted = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Residence residence;

    @ElementCollection(targetClass = Interest.class)
    @CollectionTable(
            name = "MEMBER_INTEREST",
            joinColumns = @JoinColumn(name = "MEMBER_ID")
    )

    @Enumerated(EnumType.STRING)
    @Column(name = "INTEREST", length = 30, nullable = false)
    @Builder.Default
    private Set<Interest> interests = new HashSet<>();

    public static Member createMember(
            Long userKey,
            String name,
            String phoneNumber,
            String birthDay,
            String email,
            Gender gender,
            Role role,
            MemberStatus status
    ) {
        return Member.builder()
                .userKey(userKey)
                .name(name)
                .phoneNumber(phoneNumber)
                .birthDay(birthDay)
                .email(email)
                .gender(gender)
                .role(role)
                .status(status)
                .build();
    }

    public void update(
            String name,
            String phoneNumber,
            String birthDay,
            String email,
            Gender gender,
            MemberStatus status
    ) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.birthDay = birthDay;
        this.email = email;
        this.gender = gender;
        this.status = status;
    }

    public void updateAgreePolicy(
            List<String> agreeTerms
    ) {
        if (agreeTerms == null) {
            return;
        }
        for (String term : agreeTerms) {
            if (SERVICE_AGREED.getTermName().equals(term)) {
                this.serviceAgreed = true;
            }
            else if(MARKETING_AGREED.getTermName().equals(term)) {
                this.marketingAgreed = true;
            }
        }
    }

    public void memberConnectOut() {
        this.status = TOSS_CONNECT_OUT;
    }

    public void increaseCoin(long amount) {
        if (amount <= 0) throw new CustomException(CoinErrorCode.COIN_NOT_POSITIVE);
        this.coin += amount;
    }

    public void decreaseCoin(long amount) {
        if (amount <= 0) throw new CustomException(CoinErrorCode.COIN_NOT_POSITIVE);
        if (this.coin < amount) throw new CustomException(CoinErrorCode.COIN_LACK);
        this.coin -= amount;
    }

    public void increasePromotionPoint(long amount) {
        if (amount <= 0) {
            throw new CustomException(CoinErrorCode.COIN_NOT_POSITIVE);
        }
        this.promotionPoint += amount;
    }

    public void changeProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public void completeOnboarding(Residence residence, Set<Interest> interests) {
        this.residence = residence;
        this.interests.clear();
        if (interests != null) {
            this.interests.addAll(interests);
        }
        this.onboardingCompleted = true;
    }
}
