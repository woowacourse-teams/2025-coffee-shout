package coffeeshout.room.infra;

import coffeeshout.room.domain.event.MiniGameSelectEvent;
import coffeeshout.room.domain.event.PlayerReadyEvent;
import coffeeshout.room.domain.event.RoomCreateEvent;
import coffeeshout.room.domain.event.RoomJoinEvent;
import coffeeshout.room.domain.event.RouletteSpinEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomEventPublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic roomEventTopic;

    public void publishRoomCreateEvent(RoomCreateEvent event) {
        try {
            redisTemplate.convertAndSend(roomEventTopic.getTopic(), event);
            log.info("방 생성 이벤트 발행됨: eventId={}, hostName={}", event.eventId(), event.hostName());
        } catch (Exception e) {
            log.error("방 생성 이벤트 발행 실패: eventId={}", event.eventId(), e);
        }
    }

    public void publishRoomJoinEvent(RoomJoinEvent event) {
        try {
            redisTemplate.convertAndSend(roomEventTopic.getTopic(), event);
            log.info("방 참가 이벤트 발행됨: eventId={}, joinCode={}, guestName={}",
                    event.eventId(), event.joinCode(), event.guestName());
        } catch (Exception e) {
            log.error("방 참가 이벤트 발행 실패: eventId={}", event.eventId(), e);
        }
    }

    public void publishPlayerReadyEvent(final PlayerReadyEvent event) {
        try {
            redisTemplate.convertAndSend(roomEventTopic.getTopic(), event);
            log.info("플레이어 ready 이벤트 발행됨: eventId={}, joinCode={}, playerName={}, isReady={}",
                    event.eventId(), event.joinCode(), event.playerName(), event.isReady());
        } catch (Exception e) {
            log.error("플레이어 ready 이벤트 발행 실패: eventId={}", event.eventId(), e);
        }
    }

    public void publishMiniGameSelectEvent(final MiniGameSelectEvent event) {
        try {
            redisTemplate.convertAndSend(roomEventTopic.getTopic(), event);
            log.info("미니게임 선택 이벤트 발행됨: eventId={}, joinCode={}, hostName={}, miniGameTypes={}",
                    event.eventId(), event.joinCode(), event.hostName(), event.miniGameTypes());
        } catch (final Exception e) {
            log.error("미니게임 선택 이벤트 발행 실패: eventId={}", event.eventId(), e);
        }
    }

    public void publishRouletteSpinEvent(final RouletteSpinEvent event) {
        try {
            redisTemplate.convertAndSend(roomEventTopic.getTopic(), event);
            log.info("룰렛 스핀 이벤트 발행됨: eventId={}, joinCode={}, hostName={}",
                    event.eventId(), event.joinCode(), event.hostName());
        } catch (final Exception e) {
            log.error("룰렛 스핀 이벤트 발행 실패: eventId={}", event.eventId(), e);
        }
    }
}
