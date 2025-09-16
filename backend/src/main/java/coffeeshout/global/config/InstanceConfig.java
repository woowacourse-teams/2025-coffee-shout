package coffeeshout.global.config;

import java.util.UUID;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class InstanceConfig {

    private final String instanceId;

    public InstanceConfig() {
        this.instanceId = UUID.randomUUID().toString();
    }
}
