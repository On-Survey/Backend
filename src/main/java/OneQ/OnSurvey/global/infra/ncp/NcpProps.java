package OneQ.OnSurvey.global.infra.ncp;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class NcpProps {
    @Value("${ncp.core.region}")
    private String region;

    @Value("${ncp.core.access-key}")
    private String accessKey;

    @Value("${ncp.core.secret-key}")
    private String secretKey;
}

