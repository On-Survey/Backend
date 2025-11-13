package OneQ.OnSurvey.global.infra.ncp.objectStorage.image.dto;

public record ImageUploadResponse(
        String url
) {
    public static ImageUploadResponse of(String publicUrl) {
        return new ImageUploadResponse(publicUrl);
    }
}
