package OneQ.OnSurvey.global.infra.toss.common.dto.auth;

import java.util.List;

public record LoginMeResponse(
        String resultType,
        Success success,
        String error
) {
    public record Success(
            long userKey,
            String scope,
            List<String> agreedTerms,
            String policy,
            String certTxId,
            String name,
            String phone,
            String birthday,
            String ci,
            String di, // nullable
            String gender,
            String nationality,
            String email // nullable
    ) {}
}

