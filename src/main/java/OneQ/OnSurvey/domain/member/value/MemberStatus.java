package OneQ.OnSurvey.domain.member.value;

public enum MemberStatus {
    ACTIVE, TOSS_CONNECT_OUT;

    public static boolean isSameMemberStatus(MemberStatus a, MemberStatus b) {
        return a == b;
    }
}
