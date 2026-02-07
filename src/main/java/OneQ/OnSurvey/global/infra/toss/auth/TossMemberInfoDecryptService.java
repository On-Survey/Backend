package OneQ.OnSurvey.global.infra.toss.auth;

import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.global.auth.dto.DecryptedLoginMeResponse;
import OneQ.OnSurvey.global.infra.toss.common.dto.auth.LoginMeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class TossMemberInfoDecryptService {

    @Value("${toss.decrypt.key}")
    private String base64EncodedAesKey;

    @Value("${toss.decrypt.aad}")
    private String aad;

    private static final int IV_LENGTH = 12;

    private String decrypt(
            String encryptedText
    ) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(encryptedText);
        if (decoded.length < IV_LENGTH) {
            log.warn("[TossMemberInfoDecryptService] 암호화 데이터 길이 부족: {} bytes (최소 {} 필요)", decoded.length, IV_LENGTH);
            return null;
        }
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] keyByteArray = Base64.getDecoder().decode(base64EncodedAesKey);
        SecretKeySpec key = new SecretKeySpec(keyByteArray, "AES");
        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(decoded, 0, iv, 0, IV_LENGTH);
        GCMParameterSpec nonceSpec = new GCMParameterSpec(16 * Byte.SIZE, iv);

        cipher.init(Cipher.DECRYPT_MODE, key, nonceSpec);
        cipher.updateAAD(aad.getBytes());

        byte[] decrypted = cipher.doFinal(decoded, IV_LENGTH, decoded.length - IV_LENGTH);
        return new String(decrypted);
    }

    public DecryptedLoginMeResponse decryptResponse(LoginMeResponse.Success enc) throws Exception {

        // 각 필드 복호화 (null 방지)
        String name = decryptSafe(enc.name());
        String phone = decryptSafe(enc.phone());
        String birthday = decryptSafe(enc.birthday());
        String genderStr = decryptSafe(enc.gender()); // 1단계: 복호화된 문자열
        Gender gender = toGender(genderStr);
        String nationality = decryptSafe(enc.nationality());
        String email = decryptSafe(enc.email());

        return new DecryptedLoginMeResponse(
             enc.userKey(),
             enc.scope(),
             enc.agreedTerms(),
             enc.policy(),
             enc.certTxId(),
             name,
             phone,
             birthday,
             gender,
             nationality,
             email
        );
    }

    private String decryptSafe(String encoded) throws Exception {
        if (encoded == null || encoded.isBlank()) {
            return encoded;
        }
        return decrypt(encoded);
    }

    private Gender toGender(String decrypted) {
        if (decrypted == null || decrypted.isBlank()) {
            return Gender.ALL;
        }

        try {
            return Gender.valueOf(decrypted.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}