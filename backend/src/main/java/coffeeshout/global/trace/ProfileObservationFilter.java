package coffeeshout.global.trace;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationFilter;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ProfileObservationFilter implements ObservationFilter {

    private final String profile;

    public ProfileObservationFilter(Environment env) {
        this.profile = String.join(",", env.getActiveProfiles());
    }

    @Override
    public Observation.Context map(Observation.Context context) {
        context.addLowCardinalityKeyValue(KeyValue.of("profile", profile));
        return context;
    }
}
