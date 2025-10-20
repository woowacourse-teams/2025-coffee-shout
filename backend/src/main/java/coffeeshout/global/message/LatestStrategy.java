package coffeeshout.global.message;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.stereotype.Component;

@Component
@Profile({"!local & !test & !prod"})
public class LatestStrategy implements RedisStreamStartStrategy{
    @Override
    public StreamOffset<String> getStreamOffset(String streamKey) {
        return StreamOffset.latest(streamKey);
    }
}
