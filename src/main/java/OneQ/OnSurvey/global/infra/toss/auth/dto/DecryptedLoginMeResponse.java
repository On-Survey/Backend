package OneQ.OnSurvey.global.infra.toss.auth.dto;

import java.util.List;

public record DecryptedLoginMeResponse(
        long userKey,
        String scope,
        List<String> agreedTerms,
        String policy,
        String certTxId,
        String name,
        String phone,
        String birthday,
        String gender,
        String nationality,
        String email
) {
}
