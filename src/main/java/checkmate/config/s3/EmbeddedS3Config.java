package checkmate.config.s3;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import io.findify.s3mock.S3Mock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Slf4j
@Profile("!production")
@Configuration
public class EmbeddedS3Config {
    private final int port = 8001;

    @Bean
    public S3Mock s3Mock() {
        return new S3Mock.Builder()
                .withPort(port)
                .withInMemoryBackend()
                .build();
    }

    @Bean(destroyMethod = "shutdown") @Primary
    public AmazonS3 amazonS3(){
        this.s3Mock().start();
        AwsClientBuilder.EndpointConfiguration endpoint =
                new AwsClientBuilder.EndpointConfiguration("http://localhost:" + port, Regions.AP_NORTHEAST_2.name());
        AmazonS3 client = AmazonS3ClientBuilder
                .standard()
                .withPathStyleAccessEnabled(true)
                .withEndpointConfiguration(endpoint)
                .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
                .build();
        client.createBucket("checkmate");
        return client;
    }
}
