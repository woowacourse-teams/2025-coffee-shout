package coffeeshout.global.config.redis;

import org.springframework.data.redis.connection.MessageListener;

public interface EventSubscriber extends MessageListener {

    EventTopicRegistry getTopicRegistry();
}
