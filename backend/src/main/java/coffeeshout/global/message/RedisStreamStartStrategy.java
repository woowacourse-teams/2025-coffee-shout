package coffeeshout.global.message;

import org.springframework.data.redis.connection.stream.StreamOffset;

public interface RedisStreamStartStrategy {

    StreamOffset<String> getStreamOffset(String streamKey);
}
