package OneQ.OnSurvey.global.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JwtDecodeUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Map<String, Object> decodePayload(String accessToken) {
        try {
            String[] parts = accessToken.split("\\.");
            if (parts.length < 2) {
                return Map.of("error", "Invalid JWT format");
            }

            String payload = parts[1];
            byte[] decoded = Base64.getUrlDecoder().decode(payload);
            String json = new String(decoded, StandardCharsets.UTF_8);

            return objectMapper.readValue(json, HashMap.class);
        } catch (Exception e) {
            log.warn("JWT decode 실패: {}", e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }

    public static String maskToken(String token) {
        if (token == null || token.length() <= 4) {
            return "****";
        }

        int prefixLen = Math.min(3, token.length() / 4);
        int suffixLen = Math.min(3, token.length() / 4);

        String prefix = token.substring(0, prefixLen);
        String suffix = token.substring(token.length() - suffixLen);

        return prefix + "****" + suffix;
    }

    public static boolean isTokenExpired(String accessToken) {
        Map<String, Object> payload = decodePayload(accessToken);
        if (payload.containsKey("exp")) {
            long exp = (long) payload.get("exp");
            long currentTime = System.currentTimeMillis();
            return (exp - currentTime) <= 0;
        }
        return false;
    }
}
