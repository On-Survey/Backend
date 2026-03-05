package OneQ.OnSurvey.global.infra.ncp;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NcpCredentialsProvider {

    private final NcpProps props;

    public AWSCredentials awsCredentials() {
        return new BasicAWSCredentials(props.getAccessKey(), props.getSecretKey());
    }

    public String accessKey() { return props.getAccessKey(); }
    public String secretKey() { return props.getSecretKey(); }
    public String region()    { return props.getRegion(); }
}