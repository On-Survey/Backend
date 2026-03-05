package OneQ.OnSurvey.global.infra.ncp.objectStorage.image.util;

import OneQ.OnSurvey.global.infra.ncp.objectStorage.image.enums.ImageRootFolder;
import OneQ.OnSurvey.global.infra.ncp.objectStorage.image.enums.ImageSubFolder;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class ObjectKeyFactory {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE;

    private ObjectKeyFactory() {}

    /**
     * NCP Object Storage 저장 위치
     *   rootFolder > subFolder > YYYY > MM > DD
     * Object Key
     *   rootFolder/subFolder/YYYY/MM/DD/{YYYYMMDD}{사용자ID}{UUID}
     * 이미지 파일명
     *   날짜_사용자ID_UUID
     */
    public static String build(ImageRootFolder root, ImageSubFolder sub,
                               Long userKey, String originalFilename) {
        String ymdDir = LocalDate.now(KST).toString().replace("-", "/");
        String dateStr = LocalDate.now(KST).format(DATE);

        String ext = safeExt(originalFilename);
        String who = (userKey == null) ? "anonymous" : String.valueOf(userKey);
        String uuid = UUID.randomUUID().toString().replace("-", "");

        String filename = dateStr + "_" + who + "_" + uuid + "." + ext;
        return root.dir() + "/" + sub.dir() + "/" + ymdDir + "/" + filename;
    }

    public static String safeExt(String originalName) {
        if (originalName == null) return "bin";
        int i = originalName.lastIndexOf('.');
        if (i < 0 || i == originalName.length() - 1) return "bin";
        String ext = originalName.substring(i + 1).toLowerCase();
        return (ext.length() > 6) ? "bin" : ext;
    }
}

