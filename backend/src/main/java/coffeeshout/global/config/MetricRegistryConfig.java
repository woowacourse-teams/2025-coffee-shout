package coffeeshout.global.config;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

@Configuration
public class MetricRegistryConfig {

    @Bean
    @Primary
    public CloudWatchMeterRegistry cloudWatchMeterRegistry(CloudWatchConfig cloudWatchConfig) {

        final CloudWatchAsyncClient cloudWatchClient = CloudWatchAsyncClient.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        return new CloudWatchMeterRegistry(
                cloudWatchConfig,
                Clock.SYSTEM,
                cloudWatchClient
        );
    }

    @Bean
    public CloudWatchConfig cloudWatchConfig() {
        return new CloudWatchConfig() {

            @Override
            public Duration step() {
                return Duration.ofMinutes(1);
            }

            @Override
            public boolean enabled() {
                return true;
            }

            @Override
            public String namespace() {
                return "coffee-shout-dev";
            }

            @Override
            public int batchSize() {
                return 20;
            }

            @Override
            public String get(String s) {
                return null;
            }
        };
    }
}
