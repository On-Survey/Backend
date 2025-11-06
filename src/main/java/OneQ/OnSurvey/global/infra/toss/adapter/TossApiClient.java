package OneQ.OnSurvey.global.infra.toss.adapter;

import OneQ.OnSurvey.global.exception.CustomException;
import OneQ.OnSurvey.global.infra.toss.TossErrorCode;
import OneQ.OnSurvey.global.infra.toss.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class TossApiClient {

    @Value("${toss.api.get-access-token}")
    private String getAccessTokenUrl;

    @Value("${toss.api.get-user-info}")
    private String getUserInfoUrl;

    @Value("${toss.api.promotion.get-key}")
    private String promotionGetKeyPath;

    @Value("${toss.api.promotion.execute}")
    private String promotionExecutePath;

    @Value("${toss.api.promotion.result}")
    private String promotionResultPath;

    private final ObjectMapper objectMapper;

    public SSLContext createSSLContext(String certPath, String keyPath) throws Exception {
        X509Certificate cert = loadCertificate(certPath);
        PrivateKey key = loadPrivateKey(keyPath);

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry("client-cert", cert);
        keyStore.setKeyEntry("client-key", key, new char[0], new java.security.cert.Certificate[]{cert});

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, new char[0]);

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(kmf.getKeyManagers(), null, null);
        return context;
    }

    private X509Certificate loadCertificate(String path) throws Exception {
        String content = Files.readString(Path.of(path), StandardCharsets.UTF_8)
                .replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "")
                .replaceAll("\\s", "");
        byte[] bytes = Base64.getDecoder().decode(content);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        try (InputStream in = new java.io.ByteArrayInputStream(bytes)) {
            return (X509Certificate) cf.generateCertificate(in);
        }
    }

    private PrivateKey loadPrivateKey(String path) throws Exception {
        String pem = Files.readString(Path.of(path), StandardCharsets.UTF_8).trim();

        // PKCS#8 ("-----BEGIN PRIVATE KEY-----") 가정
        String content = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(content);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

        // 필요에 따라 "EC"로 변경 (EC 키인 경우)
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public String getAccessToken(SSLContext context, TossLoginRequest tossLoginRequest) throws IOException {
        HttpsURLConnection conn = openJsonPostUrl(getAccessTokenUrl, context);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(objectMapper.writeValueAsBytes(tossLoginRequest));
        }

        String resp = readAll(conn);
        JsonNode root = objectMapper.readTree(resp);
        if (!"SUCCESS".equals(root.path("resultType").asText())) {
            throw new CustomException(TossErrorCode.TOSS_ACCESS_TOKEN_ERROR);
        }
        return root.path("success").path("accessToken").asText();
    }

    public LoginMeResponse.Success getLoginMe(SSLContext sslContext, String accessToken) throws IOException {
        HttpsURLConnection conn = (HttpsURLConnection) new URL(getUserInfoUrl).openConnection();
        conn.setSSLSocketFactory(sslContext.getSocketFactory());
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);

        String resp = readAll(conn);
        JsonNode root = objectMapper.readTree(resp);
        if (!"SUCCESS".equals(root.path("resultType").asText())) {
            throw new CustomException(TossErrorCode.TOSS_GET_USER_INFO_ERROR);
        }
        JsonNode successRoot = root.path("success");
        List<String> agreedTerms = new ArrayList<>();
        for (JsonNode termNode : successRoot.path("agreedTerms")) agreedTerms.add(termNode.asText());

        return new LoginMeResponse.Success(
                successRoot.path("userKey").asLong(),
                successRoot.path("scope").asText(),
                agreedTerms,
                successRoot.path("policy").asText(),
                successRoot.path("certTxId").asText(),
                successRoot.path("name").asText(),
                successRoot.path("phone").asText(),
                successRoot.path("birthday").asText(),
                successRoot.path("ci").asText(),
                successRoot.path("di").asText(),
                successRoot.path("gender").asText(),
                successRoot.path("nationality").asText(),
                successRoot.path("email").asText()
        );
    }

    private String readAll(HttpsURLConnection conn) throws IOException {
        InputStream in = (conn.getResponseCode() >= 400) ? conn.getErrorStream() : conn.getInputStream();
        if (in == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            for (String line; (line = br.readLine()) != null; ) sb.append(line).append('\n');
            return sb.toString();
        }
    }

    private HttpsURLConnection openJsonPostUrl(String url, SSLContext ctx) throws IOException {
        HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
        conn.setSSLSocketFactory(ctx.getSocketFactory());
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        return conn;
    }

    /** 지급 Key 발급 */
    public PromotionKeyResponse getPromotionKey(long userKey, SSLContext ctx) throws IOException {
        HttpsURLConnection conn = openJsonPostUrl(promotionGetKeyPath, ctx);
        conn.setRequestProperty("x-toss-user-key", String.valueOf(userKey));

        try (OutputStream os = conn.getOutputStream()) {
            os.write("{}".getBytes(StandardCharsets.UTF_8));
        }

        String resp = readAll(conn);
        JsonNode root = objectMapper.readTree(resp);
        if (!"SUCCESS".equals(root.path("resultType").asText())) {
            int code = root.path("error").path("code").asInt(-1);
            String msg = root.path("error").path("message").asText("unknown");
            log.error("[PromotionAPI:getPromotionKey] code={}, message={}, raw={}", code, msg, resp);
            throw new CustomException(TossErrorCode.TOSS_PROMOTION_API_ERROR);
        }
        String key = root.path("success").path("key").asText();
        return new PromotionKeyResponse(key);
    }

    /** 지급 실행 (4110 재시도, 4113 멱등 성공 처리) */
    public ExecutePromotionResponse executePromotionWithRetry(
            long userKey, String promotionCode, String key, int amount, int retries, SSLContext ctx
    ) throws Exception {
        int attempt = 0;
        while (true) {
            HttpsURLConnection conn = openJsonPostUrl(promotionExecutePath, ctx);
            conn.setRequestProperty("x-toss-user-key", String.valueOf(userKey));

            ObjectNode body = objectMapper.createObjectNode()
                    .put("promotionCode", promotionCode)
                    .put("key", key)
                    .put("amount", amount);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(objectMapper.writeValueAsBytes(body));
            }

            String resp = readAll(conn);
            JsonNode root = objectMapper.readTree(resp);
            if ("SUCCESS".equals(root.path("resultType").asText())) {
                String returnedKey = root.path("success").path("key").asText();
                return new ExecutePromotionResponse(returnedKey);
            }

            int code = root.path("error").path("code").asInt(-1);

            // 4110: 일시 오류는 짧게 재시도
            if (code == 4110 && attempt++ < retries) {
                Thread.sleep(200L * attempt);
                continue;
            }
            // 4113: 동일 key 중복은 멱등 성공 간주
            if (code == 4113) {
                return new ExecutePromotionResponse(key);
            }

            String msg = root.path("error").path("message").asText("unknown");
            log.error("[PromotionAPI:executePromotion] code={}, message={}, raw={}", code, msg, resp);
            throw new CustomException(TossErrorCode.TOSS_PROMOTION_API_ERROR);
        }
    }

    /** 지급 결과 조회 */
    public ExecutionResultResponse getPromotionResult(long userKey, String promotionCode, String key, SSLContext ctx) throws IOException {
        HttpsURLConnection conn = openJsonPostUrl(promotionResultPath, ctx);
        conn.setRequestProperty("x-toss-user-key", String.valueOf(userKey));

        ObjectNode body = objectMapper.createObjectNode()
                .put("promotionCode", promotionCode)
                .put("key", key);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(objectMapper.writeValueAsBytes(body));
        }

        String resp = readAll(conn);
        JsonNode root = objectMapper.readTree(resp);
        if (!"SUCCESS".equals(root.path("resultType").asText())) {
            int code = root.path("error").path("code").asInt(-1);
            String msg = root.path("error").path("message").asText("unknown");
            log.error("[PromotionAPI:getPromotionResult] code={}, message={}, raw={}", code, msg, resp);
            throw new CustomException(TossErrorCode.TOSS_PROMOTION_API_ERROR);
        }
        String status = root.path("success").asText();
        return new ExecutionResultResponse(status);
    }
}
