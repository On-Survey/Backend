package OneQ.OnSurvey.global.infra.ncp.objectStorage.image.service;

import OneQ.OnSurvey.global.common.exception.CustomException;
import OneQ.OnSurvey.global.infra.ncp.objectStorage.NcpS3Client;
import OneQ.OnSurvey.global.infra.ncp.objectStorage.image.ImageErrorCode;
import OneQ.OnSurvey.global.infra.ncp.objectStorage.image.dto.ImageUploadResponse;
import OneQ.OnSurvey.global.infra.ncp.objectStorage.image.enums.ImageRootFolder;
import OneQ.OnSurvey.global.infra.ncp.objectStorage.image.enums.ImageSubFolder;
import OneQ.OnSurvey.global.infra.ncp.objectStorage.image.util.ObjectKeyFactory;
import OneQ.OnSurvey.global.infra.ncp.objectStorage.url.NcpPublicUrlStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class ImageModifyService {

    private static final Set<String> ALLOWED = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/webp",
            MediaType.IMAGE_GIF_VALUE
    );

    private static final long MAX_BYTES = 10L * 1024 * 1024; // 10MB

    private static final Map<String, String> EXT_TO_CT = Map.of(
            "jpg", MediaType.IMAGE_JPEG_VALUE,
            "jpeg", MediaType.IMAGE_JPEG_VALUE,
            "png", MediaType.IMAGE_PNG_VALUE,
            "gif", MediaType.IMAGE_GIF_VALUE,
            "webp", "image/webp"
    );

    private final NcpS3Client s3Client;
    private final NcpPublicUrlStrategy urlStrategy;

    public ImageUploadResponse upload(MultipartFile file, Long userKey) {
        validate(file);

        ImageRootFolder root = ImageRootFolder.PUBLIC;
        ImageSubFolder sub = ImageSubFolder.MEMBER;

        String objectKey = ObjectKeyFactory.build(root, sub, userKey, file.getOriginalFilename());
        String contentType = chooseContentType(file);

        try (InputStream in = file.getInputStream()) {
            s3Client.putPublicObject(objectKey, contentType, file.getSize(), in);
        } catch (Exception e) {
            throw new CustomException(ImageErrorCode.UPLOAD_FAILED);
        }

        try {
            s3Client.ensurePublicReadableOrThrow(objectKey);
        } catch (CustomException infra) {
            throw new CustomException(ImageErrorCode.NOT_PUBLIC);
        }

        String publicUrl = urlStrategy.toUrl(objectKey);
        return ImageUploadResponse.of(publicUrl);
    }

    private void validate(MultipartFile f) {
        if (f == null || f.isEmpty()) throw new CustomException(ImageErrorCode.UPLOAD_FAILED);
        if (f.getSize() > MAX_BYTES) throw new CustomException(ImageErrorCode.TOO_LARGE);

        String ct = (f.getContentType() == null) ? "" : f.getContentType();
        if (ct.isEmpty()) {
            String ext = ObjectKeyFactory.safeExt(f.getOriginalFilename());
            String guessed = EXT_TO_CT.getOrDefault(ext, "");
            if (!ALLOWED.contains(guessed)) throw new CustomException(ImageErrorCode.INVALID_TYPE);
        } else {
            if (!ALLOWED.contains(ct)) throw new CustomException(ImageErrorCode.INVALID_TYPE);
        }
    }

    private String chooseContentType(MultipartFile f) {
        String ct = f.getContentType();
        if (ct != null && !ct.isBlank()) return ct;

        String ext = ObjectKeyFactory.safeExt(f.getOriginalFilename());
        return EXT_TO_CT.getOrDefault(ext, "application/octet-stream");
    }
}