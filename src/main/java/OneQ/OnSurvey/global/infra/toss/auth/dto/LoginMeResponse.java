package OneQ.OnSurvey.global.infra.toss.auth.dto;

import java.util.List;

public record LoginMeResponse(
        String resultType,
        Success success,
        String error // 실패시 내려옴
) {
    public record Success(
            long userKey,
            String scope,
            List<String> agreedTerms,
            String policy,
            String certTxId,
            String name,        // 암호화된 값
            String phone,       // 암호화된 값
            String birthday,    // 암호화된 값(yyyyMMdd)
            String ci,          // 암호화된 값
            String di,          // null 가능
            String gender,      // 암호화된 값
            String nationality, // 암호화된 값
            String email        // null 가능
    ) {}
}

