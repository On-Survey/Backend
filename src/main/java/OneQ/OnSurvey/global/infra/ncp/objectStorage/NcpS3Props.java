package OneQ.OnSurvey.global.infra.ncp.objectStorage;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class NcpS3Props {
    @Value("${ncp.objectstorage.endpoint}")
    private String endpoint;

    @Value("${ncp.objectstorage.bucket}")
    private String bucket;

    @Value("${ncp.objectstorage.public-base-url}")
    private String publicBaseUrl;
}
