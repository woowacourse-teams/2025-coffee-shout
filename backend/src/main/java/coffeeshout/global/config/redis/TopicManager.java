package coffeeshout.global.config.redis;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TopicManager {

    private final Map<EventTopicRegistry, ChannelTopic> topicCache = new ConcurrentHashMap<>();

    public ChannelTopic getTopic(EventTopicRegistry registry) {
        Objects.requireNonNull(registry, "EventTopicRegistry는 null일 수 없습니다");
        try {
            return topicCache.computeIfAbsent(registry, EventTopicRegistry::toChannelTopic);
        } catch (Exception e) {
            log.error("토픽 생성 실패: registry={}", registry, e);
            throw new IllegalStateException("토픽 생성 중 오류 발생: " + registry, e);
        }
    }

    public String getTopicName(EventTopicRegistry registry) {
        return getTopic(registry).getTopic();
    }
}
