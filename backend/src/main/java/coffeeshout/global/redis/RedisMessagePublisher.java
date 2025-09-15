package coffeeshout.global.redis;

import coffeeshout.global.redis.event.minigame.CardSelectedEvent;
import coffeeshout.global.redis.event.minigame.MiniGameCompletedEvent;
import coffeeshout.global.redis.event.minigame.MiniGameRoundProgressEvent;
import coffeeshout.global.redis.event.minigame.MiniGameStartedEvent;
import coffeeshout.global.redis.event.minigame.MiniGamesUpdatedEvent;
import coffeeshout.global.redis.event.player.HostPromotedEvent;
import coffeeshout.global.redis.event.player.PlayerJoinedEvent;
import coffeeshout.global.redis.event.player.PlayerMenuSelectedEvent;
import coffeeshout.global.redis.event.player.PlayerReadyStateChangedEvent;
import coffeeshout.global.redis.event.player.PlayerRemovedEvent;
import coffeeshout.global.redis.event.room.RoomCreatedEvent;
import coffeeshout.global.redis.event.room.RoomDeletedEvent;
import coffeeshout.global.redis.event.room.RoomStateChangedEvent;
import coffeeshout.global.redis.event.roulette.RouletteSpinEvent;
import coffeeshout.global.redis.event.websocket.WebSocketBroadcastEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisMessagePublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public void publishRoomCreated(RoomCreatedEvent event) {
        publish("room:created", event);
    }

    public void publishRoomDeleted(RoomDeletedEvent event) {
        publish("room:deleted", event);
    }

    public void publishRoomStateChanged(RoomStateChangedEvent event) {
        publish("room:state", event);
    }

    public void publishPlayerJoined(PlayerJoinedEvent event) {
        publish("player:joined", event);
    }

    public void publishPlayerRemoved(PlayerRemovedEvent event) {
        publish("player:removed", event);
    }

    public void publishPlayerMenuSelected(PlayerMenuSelectedEvent event) {
        publish("player:menu", event);
    }

    public void publishPlayerReadyStateChanged(PlayerReadyStateChangedEvent event) {
        publish("player:ready", event);
    }

    public void publishHostPromoted(HostPromotedEvent event) {
        publish("player:host", event);
    }

    public void publishMiniGamesUpdated(MiniGamesUpdatedEvent event) {
        publish("minigame:updated", event);
    }

    public void publishMiniGameStarted(MiniGameStartedEvent event) {
        publish("minigame:started", event);
    }

    public void publishCardSelected(CardSelectedEvent event) {
        publish("minigame:card:selected", event);
    }

    public void publishMiniGameRoundProgress(MiniGameRoundProgressEvent event) {
        publish("minigame:round:progress", event);
    }

    public void publishMiniGameCompleted(MiniGameCompletedEvent event) {
        publish("minigame:completed", event);
    }

    public void publishRouletteSpin(RouletteSpinEvent event) {
        publish("roulette:spin", event);
    }

    public void publishWebSocketMessage(WebSocketBroadcastEvent event) {
        publish("websocket:broadcast", event);
    }

    private void publish(String channel, Object event) {
        try {
            redisTemplate.convertAndSend(channel, event);
            log.debug("Redis 메시지 발행: channel={}, event={}", channel, event.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("Redis 메시지 발행 실패: channel={}, error={}", channel, e.getMessage(), e);
        }
    }
}
