package OneQ.OnSurvey.global.common.config;

import OneQ.OnSurvey.global.infra.ncp.NcpCredentialsProvider;
import OneQ.OnSurvey.global.infra.ncp.objectStorage.NcpS3Props;
import OneQ.OnSurvey.global.infra.ncp.objectStorage.url.NcpPathStylePublicUrlStrategy;
import OneQ.OnSurvey.global.infra.ncp.objectStorage.url.NcpPublicUrlStrategy;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class NcpObjectStorageConfig {

    private final NcpCredentialsProvider creds;
    private final NcpS3Props s3Props;

    @Bean
    public AmazonS3 amazonS3() {
        AmazonS3Client client = new AmazonS3Client(
                new AWSStaticCredentialsProvider(creds.awsCredentials()),
                new ClientConfiguration()
        );
        client.setEndpoint(s3Props.getEndpoint());
        client.setS3ClientOptions(S3ClientOptions.builder().setPathStyleAccess(true).build());
        return client;
    }

    @Bean
    public NcpPublicUrlStrategy objectUrlStrategy() {
        return new NcpPathStylePublicUrlStrategy(s3Props.getPublicBaseUrl(), s3Props.getBucket());
    }
}
