package coffeeshout.global.config;

import coffeeshout.global.config.properties.CloudWatchMetricsProperties;
import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

@Configuration
@EnableConfigurationProperties(CloudWatchMetricsProperties.class)
@RequiredArgsConstructor
public class MetricRegistryConfig {

    private final CloudWatchMetricsProperties cloudWatchMetricsProperties;

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
                return cloudWatchMetricsProperties.getStep();
            }

            @Override
            public boolean enabled() {
                return cloudWatchMetricsProperties.isEnabled();
            }

            @Override
            public String namespace() {
                return cloudWatchMetricsProperties.getNamespace();
            }

            @Override
            public int batchSize() {
                return cloudWatchMetricsProperties.getBatchSize();
            }

            @Override
            public String get(String s) {
                return null;
            }
        };
    }
}
