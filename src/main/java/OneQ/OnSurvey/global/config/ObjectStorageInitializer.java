package OneQ.OnSurvey.global.config;

import OneQ.OnSurvey.global.infra.ncp.objectStorage.NcpS3Props;
import OneQ.OnSurvey.global.infra.ncp.objectStorage.ObjectStorageErrorCode;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.BucketCrossOriginConfiguration;
import com.amazonaws.services.s3.model.CORSRule;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ObjectStorageInitializer {

    private static final String POLICY_VERSION = "2012-10-17";
    private static final String POLICY_SID = "PublicReadGetObject";
    private static final String ACTION_GET_OBJECT = "s3:GetObject";
    private static final List<String> EXPOSE_HEADERS = List.of("ETag", "Content-Length");
    private static final int CORS_MAX_AGE_SECONDS = 3600;

    private final AmazonS3 s3;
    private final NcpS3Props props;

    @PostConstruct
    public void ensurePolicyAndCors() {
        tryApplyPublicReadPolicy();
        tryApplyCors();
    }

    private void tryApplyPublicReadPolicy() {
        final String bucket = props.getBucket();
        if (isBlank(bucket)) {
            log.warn("[ObjectStorage][Policy] {}: bucket is blank, skip apply",
                    ObjectStorageErrorCode.BUCKET_NOT_CONFIGURED.getErrorCode());
            return;
        }
        try {
            s3.setBucketPolicy(bucket, buildPublicReadPolicy(bucket)); // 멱등 적용
            log.info("[ObjectStorage][Policy] applied (bucket={})", bucket);
        } catch (Exception e) {
            log.warn("[ObjectStorage][Policy] {}: apply failed (bucket={}, reason={})",
                    ObjectStorageErrorCode.POLICY_APPLY_FAILED.getErrorCode(), bucket, e.getMessage());
            log.debug("[ObjectStorage][Policy] stack:", e);
            // throw new CustomException(ObjectStorageErrorCode.POLICY_APPLY_FAILED);
        }
    }

    private String buildPublicReadPolicy(String bucket) {
        return "{\n" +
                "  \"Version\":\"" + POLICY_VERSION + "\",\n" +
                "  \"Statement\":[{\n" +
                "    \"Sid\":\"" + POLICY_SID + "\",\n" +
                "    \"Effect\":\"Allow\",\n" +
                "    \"Principal\":\"*\",\n" +
                "    \"Action\":[\"" + ACTION_GET_OBJECT + "\"],\n" +
                "    \"Resource\":[\"arn:aws:s3:::" + bucket + "/*\"]\n" +
                "  }]\n" +
                "}";
    }

    private void tryApplyCors() {
        final String bucket = props.getBucket();
        if (isBlank(bucket)) {
            log.warn("[ObjectStorage][CORS] {}: bucket is blank, skip apply",
                    ObjectStorageErrorCode.BUCKET_NOT_CONFIGURED.getErrorCode());
            return;
        }
        try {
            s3.setBucketCrossOriginConfiguration(bucket, buildCorsConfiguration());
            log.info("[ObjectStorage][CORS] applied (bucket={})", bucket);
        } catch (Exception e) {
            log.warn("[ObjectStorage][CORS] {}: apply failed (bucket={}, reason={})",
                    ObjectStorageErrorCode.CORS_APPLY_FAILED.getErrorCode(), bucket, e.getMessage());
            log.debug("[ObjectStorage][CORS] stack:", e);
            // throw new CustomException(ObjectStorageErrorCode.CORS_APPLY_FAILED);
        }
    }

    private BucketCrossOriginConfiguration buildCorsConfiguration() {
        CORSRule rule = new CORSRule()
                .withAllowedOrigins(List.of("*"))
                .withAllowedMethods(List.of(CORSRule.AllowedMethods.GET, CORSRule.AllowedMethods.HEAD))
                .withAllowedHeaders(List.of("*"))
                .withExposedHeaders(EXPOSE_HEADERS)
                .withMaxAgeSeconds(CORS_MAX_AGE_SECONDS);

        return new BucketCrossOriginConfiguration().withRules(List.of(rule));
    }
    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
}
