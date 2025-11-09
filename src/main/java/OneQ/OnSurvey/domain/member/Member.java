package OneQ.OnSurvey.domain.member;

import OneQ.OnSurvey.domain.member.value.MemberStatus;
import OneQ.OnSurvey.domain.member.value.Role;
import OneQ.OnSurvey.global.entity.BaseEntity;
import OneQ.OnSurvey.global.exception.CustomException;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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

    public static Member createMember(
            Long userKey,
            String name,
            String phoneNumber,
            String birthDay,
            String email,
            Role role,
            MemberStatus status
    ) {
        return Member.builder()
                .userKey(userKey)
                .name(name)
                .phoneNumber(phoneNumber)
                .birthDay(birthDay)
                .email(email)
                .role(role)
                .status(status)
                .coin(0L)
                .build();
    }

    public void update(
            String name,
            String phoneNumber,
            String birthDay,
            String email,
            MemberStatus status
    ) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.birthDay = birthDay;
        this.email = email;
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
}
