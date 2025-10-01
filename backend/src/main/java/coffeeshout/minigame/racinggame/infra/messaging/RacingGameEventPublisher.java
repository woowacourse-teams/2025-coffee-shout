package coffeeshout.minigame.racinggame.infra.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RacingGameEventPublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic racingGameEventTopic;

    public <T> void publishEvent(T event) {
        try {
            redisTemplate.convertAndSend(racingGameEventTopic.getTopic(), event);
            log.info("레이싱 게임 이벤트 발행됨: event={}", event);
        } catch (Exception e) {
            log.error("레이싱 게임 이벤트 발행 실패: event={}", event, e);
            throw new RuntimeException("레이싱 게임 이벤트 발행 실패", e);
        }
    }
}
