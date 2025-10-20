package OneQ.OnSurvey.global.infra.toss.adapter;

import OneQ.OnSurvey.global.exception.CustomException;
import OneQ.OnSurvey.global.infra.toss.dto.LoginMeResponse;
import OneQ.OnSurvey.global.infra.toss.dto.TossLoginRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static OneQ.OnSurvey.global.infra.toss.TossErrorCode.TOSS_ACCESS_TOKEN_ERROR;
import static OneQ.OnSurvey.global.infra.toss.TossErrorCode.TOSS_GET_USER_INFO_ERROR;

@Component
@Slf4j
public class TossApiClient {

    @Value("${toss.api.get-access-token}")
    private String getAccessTokenUrl;

    @Value("${toss.api.get-user-info}")
    private String getUserInfoUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();
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

    public String makeRequest(String url, SSLContext context) throws IOException {
        HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
        conn.setSSLSocketFactory(context.getSocketFactory());
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        try {
            InputStream in = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();
            return readAll(in);
        } finally {
            conn.disconnect();
        }
    }

    public String getAccessToken(SSLContext context, TossLoginRequest tossLoginRequest) throws IOException {
        String body = objectMapper.writeValueAsString(tossLoginRequest);
        HttpsURLConnection conn = (HttpsURLConnection) new URL(getAccessTokenUrl).openConnection();
        conn.setSSLSocketFactory(context.getSocketFactory());
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        conn.setDoOutput(true); // 바디 전송 허용
        conn.setRequestProperty("Content-Type", "application/json");

        try {
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            InputStream in = (code >= 200 && code < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String resp = readAll(in);

            JsonNode root = objectMapper.readTree(resp);
            if (!"SUCCESS".equals(root.path("resultType").asText())) {
                throw new CustomException(TOSS_ACCESS_TOKEN_ERROR);
            }
            return root.path("success").path("accessToken").asText();
        } finally {
            conn.disconnect();
        }
    }

    public LoginMeResponse.Success getLoginMe(SSLContext sslContext, String accessToken) throws IOException {
        HttpsURLConnection conn = (HttpsURLConnection) new URL(getUserInfoUrl).openConnection();
        conn.setSSLSocketFactory(sslContext.getSocketFactory());
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        // 헤더
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);

        try {
            int code = conn.getResponseCode();
            InputStream in = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            String resp = readAll(in);

            JsonNode root = objectMapper.readTree(resp);
            if (!"SUCCESS".equals(root.path("resultType").asText())) {
                throw new CustomException(TOSS_GET_USER_INFO_ERROR);
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

    private String readAll(InputStream in) throws IOException {
        if (in == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
            return sb.toString();
        }
    }

}
