package coffeeshout.global.config.properties;

import java.time.Duration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "metrics.cloudwatch")
@Data
public class CloudWatchMetricsProperties {
    private String namespace = "coffee-shout-local";
    private Duration step = Duration.ofMinutes(1);
    private boolean enabled = true;
    private int batchSize = 20;
}
