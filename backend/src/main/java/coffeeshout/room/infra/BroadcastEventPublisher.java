package coffeeshout.room.infra;

import coffeeshout.room.ui.event.BroadcastEvent;
import coffeeshout.room.ui.event.ErrorBroadcastEvent;
import coffeeshout.room.ui.event.MiniGameUpdateBroadcastEvent;
import coffeeshout.room.ui.event.PlayerUpdateBroadcastEvent;
import coffeeshout.room.ui.event.ProbabilityUpdateBroadcastEvent;
import coffeeshout.room.ui.event.WinnerAnnouncementBroadcastEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BroadcastEventPublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic broadcastEventTopic;

    public void publishPlayerUpdateEvent(PlayerUpdateBroadcastEvent event) {
        publishEvent(event, "플레이어 업데이트");
    }

    public void publishProbabilityUpdateEvent(ProbabilityUpdateBroadcastEvent event) {
        publishEvent(event, "확률 업데이트");
    }

    public void publishMiniGameUpdateEvent(MiniGameUpdateBroadcastEvent event) {
        publishEvent(event, "미니게임 업데이트");
    }

    public void publishWinnerAnnouncementEvent(WinnerAnnouncementBroadcastEvent event) {
        publishEvent(event, "당첨자 발표");
    }

    public void publishErrorEvent(ErrorBroadcastEvent event) {
        publishEvent(event, "에러");
    }

    private void publishEvent(BroadcastEvent event, String eventName) {
        try {
            redisTemplate.convertAndSend(broadcastEventTopic.getTopic(), event);
            log.info("{} 브로드캐스트 이벤트 발행됨: joinCode={}, type={}",
                    eventName, event.joinCode(), event.getBroadcastEventType());
        } catch (final Exception e) {
            log.error("{} 브로드캐스트 이벤트 발행 실패: joinCode={}", eventName, event.joinCode(), e);
        }
    }
}
