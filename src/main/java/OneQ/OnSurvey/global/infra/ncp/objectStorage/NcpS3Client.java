package OneQ.OnSurvey.global.infra.ncp.objectStorage;

import OneQ.OnSurvey.global.common.exception.CustomException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class NcpS3Client {

    private final AmazonS3 s3;
    private final NcpS3Props props;

    public void putPublicObject(String key, String contentType, long size, InputStream in) {
        final String bucket = props.getBucket();
        if (isBlank(bucket)) {
            log.error("[ObjectStorage][Upload] bucket is blank (key={})", key);
            throw new CustomException(ObjectStorageErrorCode.BUCKET_NOT_CONFIGURED);
        }
        if (isBlank(key)) {
            log.warn("[ObjectStorage][Upload] invalid object key (blank)");
            throw new CustomException(ObjectStorageErrorCode.INVALID_OBJECT_KEY);
        }

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(size);
        if (contentType != null) meta.setContentType(contentType);

        try {
            PutObjectRequest req = new PutObjectRequest(bucket, key, in, meta)
                    .withCannedAcl(CannedAccessControlList.PublicRead);
            s3.putObject(req);
            log.info("[ObjectStorage][Upload] success (bucket={}, key={})", bucket, key);
        } catch (AmazonS3Exception e) {
            log.warn("[ObjectStorage][Upload] failed (bucket={}, key={}, status={}, err={})",
                    bucket, key, e.getStatusCode(), e.getErrorMessage());
            throw new CustomException(ObjectStorageErrorCode.UPLOAD_FAILED);
        } catch (Exception e) {
            log.warn("[ObjectStorage][Upload] failed (bucket={}, key={}, reason={})", bucket, key, e.getMessage());
            throw new CustomException(ObjectStorageErrorCode.UPLOAD_FAILED);
        }
    }

    public void ensurePublicReadableOrThrow(String key) {
        final String bucket = props.getBucket();
        if (isBlank(bucket)) {
            log.error("[ObjectStorage][Head] bucket is blank (key={})", key);
            throw new CustomException(ObjectStorageErrorCode.BUCKET_NOT_CONFIGURED);
        }
        if (isBlank(key)) {
            log.warn("[ObjectStorage][Head] invalid object key (blank)");
            throw new CustomException(ObjectStorageErrorCode.INVALID_OBJECT_KEY);
        }

        try {
            s3.getObjectMetadata(bucket, key);
            log.info("[ObjectStorage][Head] readable (bucket={}, key={})", bucket, key);
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 403 || e.getStatusCode() == 404) {
                log.warn("[ObjectStorage][Head] not public (bucket={}, key={}, status={})",
                        bucket, key, e.getStatusCode());
                throw new CustomException(ObjectStorageErrorCode.NOT_PUBLIC);
            }
            log.warn("[ObjectStorage][Head] failed (bucket={}, key={}, status={}, err={})",
                    bucket, key, e.getStatusCode(), e.getErrorMessage());
            throw new CustomException(ObjectStorageErrorCode.HEAD_FAILED);
        } catch (Exception e) {
            log.warn("[ObjectStorage][Head] failed (bucket={}, key={}, reason={})", bucket, key, e.getMessage());
            throw new CustomException(ObjectStorageErrorCode.HEAD_FAILED);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}