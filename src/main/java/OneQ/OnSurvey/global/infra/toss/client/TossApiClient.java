package OneQ.OnSurvey.global.infra.toss.client;

import OneQ.OnSurvey.global.auth.port.out.TossAuthPort;
import OneQ.OnSurvey.global.common.exception.CustomException;
import OneQ.OnSurvey.global.infra.discord.notifier.AlertNotifier;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.PushAlimAlert;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.TossAccessTokenAlert;
import OneQ.OnSurvey.global.infra.toss.common.dto.auth.LoginMeResponse;
import OneQ.OnSurvey.global.infra.toss.common.dto.auth.TossLoginRequest;
import OneQ.OnSurvey.global.infra.toss.common.dto.auth.TossTokenResponse;
import OneQ.OnSurvey.global.infra.toss.common.dto.iap.GetOrderStatusRequest;
import OneQ.OnSurvey.global.infra.toss.common.dto.iap.OrderStatusResponse;
import OneQ.OnSurvey.global.infra.toss.common.dto.promotion.ExecutePromotionResponse;
import OneQ.OnSurvey.global.infra.toss.common.dto.promotion.ExecutionResultResponse;
import OneQ.OnSurvey.global.infra.toss.common.dto.promotion.PromotionKeyResponse;
import OneQ.OnSurvey.global.infra.toss.common.dto.push.PushResultResponse;
import OneQ.OnSurvey.global.infra.toss.common.dto.push.PushTemplateSendRequest;
import OneQ.OnSurvey.global.infra.toss.common.exception.TossApiException;
import OneQ.OnSurvey.global.infra.toss.common.exception.TossErrorCode;
import OneQ.OnSurvey.global.payment.port.out.TossIapPort;
import OneQ.OnSurvey.global.promotion.port.out.TossPromotionPort;
import OneQ.OnSurvey.global.push.application.port.out.TossPushPort;
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
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class TossApiClient implements TossAuthPort, TossIapPort, TossPromotionPort, TossPushPort {

    private static final int CONNECT_TIMEOUT_MS = 5_000;
    private static final int READ_TIMEOUT_MS = 5_000;
    private static final String HDR_AUTH = "Authorization";
    private static final String HDR_CONTENT_TYPE = "Content-Type";
    private static final String CT_JSON = "application/json";
    private static final String RESULT_SUCCESS = "SUCCESS";

    @Value("${toss.api.get-access-token}")
    private String getAccessTokenUrl;

    @Value("${toss.api.refresh-token-url}")
    private String refreshTokenUrl;

    @Value("${toss.api.remove-by-user-key-url}")
    private String removeByUserKeyUrl;

    @Value("${toss.api.remove-by-access-token-url}")
    private String removeByAccessTokenUrl;

    @Value("${toss.api.get-user-info}")
    private String getUserInfoUrl;

    @Value("${toss.api.promotion.get-key}")
    private String promotionGetKeyUrl;

    @Value("${toss.api.promotion.execute}")
    private String promotionExecuteUrl;

    @Value("${toss.api.promotion.result}")
    private String promotionResultUrl;

    @Value("${toss.api.iap.get-order-status}")
    private String getIapOrderStatusUrl;

    @Value("${toss.api.send-message-url}")
    private String sendMessageUrl;

    private final ObjectMapper objectMapper;
    private final AlertNotifier alertNotifier;

    /* ===================== SSL ===================== */
    @Override
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
        try (InputStream in = new ByteArrayInputStream(bytes)) {
            return (X509Certificate) cf.generateCertificate(in);
        }
    }

    private PrivateKey loadPrivateKey(String path) throws Exception {
        String pem = Files.readString(Path.of(path), StandardCharsets.UTF_8).trim();
        String content = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(content);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    /* ===================== OAuth ===================== */
    @Override
    public TossTokenResponse getAccessToken(SSLContext ctx, TossLoginRequest req) throws IOException {
        HttpsURLConnection conn = open(getAccessTokenUrl, ctx, "POST", true);
        try {
            writeJson(conn, req);
            int status = conn.getResponseCode();
            JsonNode root = readJson(conn);

            if (!isSuccess(root)) {
                int errCode = root.path("error").path("code").asInt(-1);
                String errMsg = root.path("error").path("message").asText("unknown");
                log.error("[TOSS_OAUTH_TOKEN_FAILED] httpStatus={} code={} msg={} resp={}",
                        status, errCode, errMsg, root);
                throw new CustomException(TossErrorCode.TOSS_ACCESS_TOKEN_ERROR);
            }

            JsonNode s = root.path("success");
            return new TossTokenResponse(
                    s.path("accessToken").asText(),
                    s.path("refreshToken").asText(null),
                    s.path("expiresIn").isNumber() ? s.path("expiresIn").asLong() : null,
                    s.path("tokenType").asText(null),
                    s.path("scope").asText(null)
            );
        } finally {
            conn.disconnect();
        }
    }

    @Override
    public TossTokenResponse refreshOauth2Token(SSLContext ctx, String refreshToken) throws IOException {
        HttpsURLConnection conn = open(refreshTokenUrl, ctx, "POST", true);
        try {
            ObjectNode body = objectMapper.createObjectNode().put("refreshToken", refreshToken);
            writeJson(conn, body);
            JsonNode root = readJson(conn);
            if (!isSuccess(root)) throw new CustomException(TossErrorCode.TOSS_ACCESS_TOKEN_ERROR);
            JsonNode s = root.path("success");
            return new TossTokenResponse(
                    s.path("accessToken").asText(),
                    s.path("refreshToken").asText(null),
                    s.path("expiresIn").isNumber() ? s.path("expiresIn").asLong() : null,
                    s.path("tokenType").asText(null),
                    s.path("scope").asText(null)
            );
        } finally {
            conn.disconnect();
        }
    }


    /* ===================== 연결 끊기 ===================== */
    @Override
    public boolean removeByAccessToken(SSLContext ctx, String accessToken) throws IOException {
        HttpsURLConnection conn = open(removeByAccessTokenUrl, ctx, "POST", true);
        conn.setRequestProperty(HDR_AUTH, "Bearer " + accessToken);
        try {
            JsonNode root = readJson(conn); // body 없이 헤더만으로 처리하는 구현 고려
            return isSuccess(root);
        } finally {
            conn.disconnect();
        }
    }

    @Override
    public boolean removeByUserKey(SSLContext ctx, long userKey) throws IOException {
        HttpsURLConnection conn = open(removeByUserKeyUrl, ctx, "POST", true);
        try {
            ObjectNode body = objectMapper.createObjectNode().put("userKey", userKey);
            writeJson(conn, body);
            JsonNode root = readJson(conn);
            return isSuccess(root);
        } finally {
            conn.disconnect();
        }
    }

    @Override
    public LoginMeResponse.Success getLoginMe(SSLContext ctx, String accessToken) throws IOException {
        HttpsURLConnection conn = open(getUserInfoUrl, ctx, "GET", false);
        conn.setRequestProperty(HDR_AUTH, "Bearer " + accessToken);
        try {
            JsonNode root = readJson(conn);
            if (!isSuccess(root)) {
                JsonNode errorNode = root.path("error");
                log.warn("[TOSS:CLIENT] 사용자 정보를 조회에 실패했습니다. - errorCode: {}, reason: {}",
                    errorNode.path("errorCode").asText(), errorNode.path("reason").asText()
                );
                alertNotifier.sendTossAccessTokenAsync(
                    new TossAccessTokenAlert(
                        accessToken,
                        errorNode.path("errorCode").asText(),
                        errorNode.path("reason").asText()
                    )
                );
                throw new CustomException(TossErrorCode.TOSS_GET_USER_INFO_ERROR);
            }
            JsonNode successRoot = root.path("success");


            List<String> agreedTerms = new ArrayList<>();
            for (JsonNode termNode : successRoot.path("agreedTerms")) {
                agreedTerms.add(termNode.asText());
            }


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
        } finally {
            conn.disconnect();
        }
    }

    /* ===================== 프로모션 ===================== */
    @Override
    public PromotionKeyResponse getPromotionKey(long userKey, SSLContext ctx) throws IOException {
        HttpsURLConnection conn = open(promotionGetKeyUrl, ctx, "POST", true);
        conn.setRequestProperty("x-toss-user-key", String.valueOf(userKey));
        try {
            writeRaw(conn, "{}");
            JsonNode root = readJson(conn);
            if (!isSuccess(root)) {
                int code = root.path("error").path("code").asInt(-1);
                String msg = root.path("error").path("message").asText("unknown");
                log.error("[PromotionAPI:getPromotionKey] code={}, message={}, raw={}", code, msg, root);
                throw new CustomException(TossErrorCode.TOSS_PROMOTION_API_ERROR);
            }
            String key = root.path("success").path("key").asText();
            return new PromotionKeyResponse(key);
        } finally {
            conn.disconnect();
        }
    }

    /** 지급 실행 (4110 재시도, 4113 멱등 성공 처리) */
    @Override
    public ExecutePromotionResponse executePromotionWithRetry(
            long userKey, String promotionCode, String key, int amount, int retries, SSLContext ctx
    ) throws Exception {
        int attempt = 0;
        while (true) {
            HttpsURLConnection conn = open(promotionExecuteUrl, ctx, "POST", true);
            conn.setRequestProperty("x-toss-user-key", String.valueOf(userKey));

            ObjectNode body = objectMapper.createObjectNode()
                    .put("promotionCode", promotionCode)
                    .put("key", key)
                    .put("amount", amount);
            try {
                writeJson(conn, body);
                JsonNode root = readJson(conn);
                if (isSuccess(root)) {
                    String returnedKey = root.path("success").path("key").asText();
                    return new ExecutePromotionResponse(returnedKey);
                }
                int code = root.path("error").path("code").asInt(-1);
                if (code == 4110 && attempt++ < retries) {
                    Thread.sleep(200L * attempt); // 지수 백오프
                    continue;
                }
                if (code == 4113) {
                    return new ExecutePromotionResponse(key); // 멱등 성공 간주
                }
                String msg = root.path("error").path("message").asText("unknown");
                log.error("[PromotionAPI:executePromotion] code={}, msg={}, raw={}", code, msg, root);
                throw new TossApiException(code, msg, root.toString());
            } finally {
                conn.disconnect();
            }
        }
    }

    @Override
    public ExecutionResultResponse getPromotionResult(long userKey, String promotionCode, String key, SSLContext ctx) throws IOException {
        HttpsURLConnection conn = open(promotionResultUrl, ctx, "POST", true);
        conn.setRequestProperty("x-toss-user-key", String.valueOf(userKey));
        try {
            ObjectNode body = objectMapper.createObjectNode()
                    .put("promotionCode", promotionCode)
                    .put("key", key);
            writeJson(conn, body);
            JsonNode root = readJson(conn);
            if (!isSuccess(root)) {
                int code = root.path("error").path("code").asInt(-1);
                String msg = root.path("error").path("message").asText("unknown");
                log.error("[PromotionAPI:getPromotionResult] code={}, msg={}, raw={}", code, msg, root);
                throw new TossApiException(code, msg, root.toString());
            }
            String status = root.path("success").asText();
            return new ExecutionResultResponse(status);
        } finally {
            conn.disconnect();
        }
    }

    /* ===================== IAP ===================== */
    @Override
    public OrderStatusResponse getIapOrderStatus(SSLContext ctx, long userKey, String orderId) throws IOException {
        HttpsURLConnection conn = open(getIapOrderStatusUrl, ctx, "POST", true);
        conn.setRequestProperty("x-toss-user-key", String.valueOf(userKey));
        try {
            writeJson(conn, new GetOrderStatusRequest(orderId));
            JsonNode root = readJson(conn);
            if (!isSuccess(root)) {
                int code = root.path("error").path("code").asInt(-1);
                String msg = root.path("error").path("message").asText("unknown");
                log.error("[IAP:getOrderStatus] code={}, message={}, raw={}", code, msg, root);
                throw new CustomException(TossErrorCode.TOSS_IAP_GET_STATUS_ERROR);
            }
            JsonNode successRoot = root.path("success");
            return OrderStatusResponse.of(
                    successRoot.path("orderId").asText(null),
                    successRoot.path("sku").asText(null),
                    successRoot.path("status").asText(null),
                    successRoot.path("reason").asText(null),
                    successRoot.path("statusDeterminedAt").asText(null)
            );
        } finally {
            conn.disconnect();
        }
    }

    /* ===================== 푸시알림 ===================== */
    @Override
    public PushResultResponse sendPush(SSLContext ctx, PushTemplateSendRequest request) throws IOException {
        long userKey = request.userKey();
        String templateSetCode = request.templateSetCode();
        Map<String, String> templateCtx = request.templateCtx();

        HttpsURLConnection conn = open(sendMessageUrl, ctx, "POST", true);
        conn.setRequestProperty("x-toss-user-key", String.valueOf(userKey));

        try {
             ObjectNode body = objectMapper.createObjectNode()
                 .put("templateSetCode", templateSetCode)
                 .set("context", objectMapper.valueToTree(templateCtx));
             writeJson(conn, body);
             JsonNode root = readJson(conn);

            JsonNode successRoot = root.path("success");
            if (!isSuccess(root)) {
                int code = root.path("error").path("errorType").asInt(-1);
                String msg = root.path("error").path("errorCode").asText("unknown");
                log.error("[PushAPI:sendPush] code={}, message={}, raw={}", code, msg, root);
                alertNotifier.sendPushAlimAsync(
                    new PushAlimAlert(
                        userKey,
                        templateSetCode,
                        successRoot.path("sentPushCount").asLong(0),
                        successRoot.path("fail").path("sentPush").size(),
                        root.path("error").path("reason").asText("unknown")
                    )
                );
                throw new CustomException(TossErrorCode.TOSS_PUSH_SEND_ERROR);
            }
            alertNotifier.sendPushAlimAsync(
                new PushAlimAlert(
                    userKey,
                    templateSetCode,
                    successRoot.path("sentPushCount").asLong(0),
                    successRoot.path("fail").path("sentPush").size(),
                    "none"
                )
            );

            return PushResultResponse.of(
                successRoot.path("sentPushCount").asLong(),
                successRoot.path("detail").path("sentPush").toPrettyString()
            );
        } finally {
            conn.disconnect();
        }
    }


    private HttpsURLConnection open(String url, SSLContext ctx, String method, boolean doOutput) throws IOException {
        HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
        conn.setSSLSocketFactory(ctx.getSocketFactory());
        conn.setRequestMethod(method);
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);
        conn.setDoOutput(doOutput);
        conn.setRequestProperty(HDR_CONTENT_TYPE, CT_JSON);
        return conn;
    }

    private void writeJson(HttpsURLConnection conn, Object body) throws IOException {
        try (OutputStream os = conn.getOutputStream()) {
            os.write(objectMapper.writeValueAsBytes(body));
        }
    }

    private void writeRaw(HttpsURLConnection conn, String body) throws IOException {
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
    }

    private JsonNode readJson(HttpsURLConnection conn) throws IOException {
        int code = conn.getResponseCode();
        InputStream in = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
        if (in == null) return objectMapper.createObjectNode();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            String payload = sb.toString();
            return payload.isBlank() ? objectMapper.createObjectNode() : objectMapper.readTree(payload);
        }
    }

    private boolean isSuccess(JsonNode root) {
        return RESULT_SUCCESS.equals(root.path("resultType").asText());
    }
}
