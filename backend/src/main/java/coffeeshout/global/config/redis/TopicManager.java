package coffeeshout.global.config.redis;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Component
public class TopicManager {

    private final Map<EventTopicRegistry, ChannelTopic> topicCache = new ConcurrentHashMap<>();

    public ChannelTopic getTopic(EventTopicRegistry registry) {
        return topicCache.computeIfAbsent(registry, EventTopicRegistry::toChannelTopic);
    }
}
