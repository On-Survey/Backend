package OneQ.OnSurvey.global.infra.ncp.objectStorage.url;

public class NcpPathStylePublicUrlStrategy implements NcpPublicUrlStrategy {
    private final String base;
    private final String bucket;

    public NcpPathStylePublicUrlStrategy(String base, String bucket) {
        this.base = base;
        this.bucket = bucket;
    }
    @Override
    public String toUrl(String key) {
        String b = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        return b + "/" + bucket + "/" + key;
    }
}

