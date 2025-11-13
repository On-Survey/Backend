package OneQ.OnSurvey.global.infra.ncp.objectStorage.url;

public interface NcpPublicUrlStrategy {
    String toUrl(String objectKey);
}
