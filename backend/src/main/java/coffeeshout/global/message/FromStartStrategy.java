package coffeeshout.global.message;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.stereotype.Component;

@Component
@Profile({"test", "local"})
public class FromStartStrategy implements RedisStreamStartStrategy {

    @Override
    public StreamOffset<String> getStreamOffset(String streamKey) {
        return StreamOffset.fromStart(streamKey);
    }
}
